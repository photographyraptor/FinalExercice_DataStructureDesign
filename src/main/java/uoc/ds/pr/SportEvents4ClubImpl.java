package uoc.ds.pr;

import java.time.LocalDate;

import edu.uoc.ds.adt.helpers.Position;
import edu.uoc.ds.adt.nonlinear.DictionaryAVLImpl;
import edu.uoc.ds.adt.nonlinear.HashTable;
import edu.uoc.ds.adt.nonlinear.PriorityQueue;
import edu.uoc.ds.adt.nonlinear.graphs.DirectedGraph;
import edu.uoc.ds.adt.nonlinear.graphs.DirectedGraphImpl;
import edu.uoc.ds.adt.nonlinear.graphs.Edge;
import edu.uoc.ds.adt.nonlinear.graphs.Vertex;
import edu.uoc.ds.adt.sequential.LinkedList;
import edu.uoc.ds.traversal.Iterator;
import uoc.ds.pr.exceptions.*;
import uoc.ds.pr.model.*;
import uoc.ds.pr.util.OrderedVector;

public class SportEvents4ClubImpl implements SportEvents4Club {
    private DictionaryAVLImpl<String, Player> players;
    private DirectedGraph<Player, Player> followers;
    private Player mostActivePlayer;
    private HashTable<String, OrganizingEntity> organizingEntities;
    private PriorityQueue<File> files;
    private DictionaryAVLImpl<String, SportEvent> sportEvents;
    private OrderedVector<SportEvent> bestSportEvent;
    private OrderedVector<OrganizingEntity> best5OrganizingEntities;
    private Role[] roles;
    private int numRoles;
    
    private int totalFiles;
    private int rejectedFiles;

    public SportEvents4ClubImpl() {
        this.players = new DictionaryAVLImpl<String, Player>();
        this.followers = new DirectedGraphImpl<Player, Player>();
        this.mostActivePlayer = null;
        this.organizingEntities = new HashTable<String, OrganizingEntity>();
        this.files = new PriorityQueue<>();
        this.sportEvents = new DictionaryAVLImpl<String, SportEvent>();
        this.bestSportEvent = new OrderedVector<SportEvent>(MAX_NUM_SPORT_EVENTS, SportEvent.CMP_V);
        this.best5OrganizingEntities = new OrderedVector<OrganizingEntity>(MAX_ORGANIZING_ENTITIES_WITH_MORE_ATTENDERS, OrganizingEntity.CMP_O);
        this.roles = new Role[MAX_ROLES];
        this.numRoles = 0;
        this.totalFiles = 0;
        this.rejectedFiles = 0;
    }

    @Override
    public void addPlayer(String id, String name, String surname, LocalDate dateOfBirth) {
        Player p = new Player(id, name, surname, dateOfBirth);        
        players.put(id, p);
    }

    @Override
    public void addOrganizingEntity(String id, String name, String description) {
        OrganizingEntity o = new OrganizingEntity(id, name, description);
        
        if (this.organizingEntities.containsKey(id)) {
            organizingEntities.delete(id);
        }

        organizingEntities.put(id, o);     
    }

    @Override
    public void addFile(String id, String eventId, String orgId, String description, Type type, byte resources, int max, LocalDate startDate, LocalDate endDate) throws OrganizingEntityNotFoundException {
        OrganizingEntity o = getOrganizingEntity(orgId);
        if (o == null){
            throw new OrganizingEntityNotFoundException();
        }
        File f = new File(id, eventId, description, type, startDate, endDate, resources, max, o);
        files.add(f);
        totalFiles++;
    }

    @Override
    public File updateFile(Status status, LocalDate date, String description) throws NoFilesException {
        File file = files.poll();
        if (file  == null) {
            throw new NoFilesException();
        }

        file.update(status, date, description);
        if (file.isEnabled()) {
            SportEvent sportEvent = file.newSportEvent();
            sportEvents.put(sportEvent.getEventId(), sportEvent);
        }
        else {
            rejectedFiles++;
        }
        return file;
    }

    @Override
    public void signUpEvent(String playerId, String eventId) throws PlayerNotFoundException, SportEventNotFoundException, LimitExceededException {
        Player player = getPlayer(playerId);
        if (player == null) {
            throw new PlayerNotFoundException();
        }

        SportEvent sportEvent = getSportEvent(eventId);
        if (sportEvent == null) {
            throw new SportEventNotFoundException();
        }

        Post p = new Post(String.format("{'player': '%s', 'sportEvent': '%s', 'action': 'signup'}", playerId, eventId));
        player.addPost(p);

        player.addEvent(sportEvent);
        if (!sportEvent.isFull()) {
            sportEvent.addEnrollment(player);
        }
        else {
            sportEvent.addSubstitute(player);
            throw new LimitExceededException();
        }

        updateMostActivePlayer(player);
    }

    @Override
    public double getRejectedFiles() {
        return (double) rejectedFiles / totalFiles;
    }

    @Override
    public Iterator<SportEvent> getSportEventsByOrganizingEntity(String organizationId) throws NoSportEventsException {
        OrganizingEntity organizingEntity = getOrganizingEntity(organizationId);

        if (organizingEntity == null || !organizingEntity.hasActivities()) {
            throw new NoSportEventsException();
        }
        return organizingEntity.sportEvents();
    }

    @Override
    public Iterator<SportEvent> getAllEvents() throws NoSportEventsException {
        Iterator<SportEvent> it = sportEvents.values();
        if (!it.hasNext()) throw new NoSportEventsException();
        return it;
    }

    @Override
    public Iterator<SportEvent> getEventsByPlayer(String playerId) throws NoSportEventsException {
        Player player = getPlayer(playerId);
        if (player == null || !player.hasEvents()) {
            throw new NoSportEventsException();
        }
        Iterator<SportEvent> it = player.getEvents();
        return it;
    }

    @Override
    public void addRating(String playerId, String eventId, Rating rating, String message) throws SportEventNotFoundException, PlayerNotFoundException, PlayerNotInSportEventException {
        SportEvent sportEvent = getSportEvent(eventId);
        if (sportEvent == null) {
            throw new SportEventNotFoundException();
        }

        Player player = getPlayer(playerId);
        if (player == null) {
            throw new PlayerNotFoundException();
        }

        if (!player.isInSportEvent(eventId)) {
            throw new PlayerNotInSportEventException();
        }

        Post p = new Post(String.format("{'player': '%s', 'sportEvent': '%s', 'rating': '%s', 'action': 'rating'}", playerId, eventId, rating.toString()));
        player.addPost(p);

        sportEvent.addRating(rating, message, player);
        updateBestSportEvent(sportEvent);
    }

    @Override
    public Iterator<uoc.ds.pr.model.Rating> getRatingsByEvent(String eventId) throws SportEventNotFoundException, NoRatingsException {
        SportEvent sportEvent = getSportEvent(eventId);
        if (sportEvent  == null) {
        	throw new SportEventNotFoundException();
        }

        if (!sportEvent.hasRatings()) {
        	throw new NoRatingsException();
        }

        return sportEvent.ratings();
    }

    @Override
    public Player mostActivePlayer() throws PlayerNotFoundException {
        if (this.mostActivePlayer == null) {
        	throw new PlayerNotFoundException();
        }
        return this.mostActivePlayer;
    }

    @Override
    public SportEvent bestSportEvent() throws SportEventNotFoundException {
        if (bestSportEvent.size() == 0) {
        	throw new SportEventNotFoundException();
        }
        return bestSportEvent.elementAt(0);
    }

    @Override
    public void addRole(String roleId, String description) {
        Role r = getRole(roleId);
        if (r != null) {
            r.setDescription(description);
        } else {
            r = new Role(roleId, description);
            roles[numRoles++] = r;
        }
    }

    @Override
    public void addWorker(String dni, String name, String surname, LocalDate birthDay, String roleId) {
        Role r = getRole(roleId);
        Worker old_w = getWorker(dni);
        Worker new_w = new Worker(dni, name, surname, birthDay, roleId);    
        
        if (old_w == null) {
            r.getWorkers().insertEnd(new_w);
        }
        else if (old_w.getRoleId() == new_w.getRoleId()) {
            r.updateWorker(r, new_w);
        }
        else {
            Role oldRole = getRole(old_w.getRoleId());
            r.updateWorker(oldRole, new_w);
        }
    }
    
    @Override
    public void assignWorker(String dni, String eventId) throws WorkerNotFoundException, WorkerAlreadyAssignedException, SportEventNotFoundException {
        if (!sportEvents.containsKey(eventId)){
            throw new SportEventNotFoundException();
        }

        Worker w = getWorker(dni);

        if (w == null) {
            throw new WorkerNotFoundException();
        }

        SportEvent s = sportEvents.get(eventId);

        if (s.getWorkerByDni(dni) != null){
            throw new WorkerAlreadyAssignedException();
        } else {
            s.addWorker(w);
        }
    }

    @Override
    public Iterator<Worker> getWorkersBySportEvent(String eventId) throws SportEventNotFoundException, NoWorkersException {
        if (!sportEvents.containsKey(eventId)){
            throw new SportEventNotFoundException();
        }

        SportEvent s = sportEvents.get(eventId);

        if (s.getWorkers().isEmpty()) {
            throw new NoWorkersException();
        } else {
            return s.getWorkers().values();
        }
    }

    @Override
    public Iterator<Worker> getWorkersByRole(String roleId) throws NoWorkersException {
        Role r = getRole(roleId);
        LinkedList<Worker> workers = r.getWorkers();

        if (workers.isEmpty()){
            throw new NoWorkersException();
        }
        
        return workers.values();        
    }

    @Override
    public Level getLevel(String playerId) throws PlayerNotFoundException {
        Player p = getPlayer(playerId);
        if (p == null) {
            throw new PlayerNotFoundException();
        } else {
            return p.getLevel();
        }
    }

    @Override
    public Iterator<Enrollment> getSubstitutes(String eventId) throws SportEventNotFoundException, NoSubstitutesException {
        if (!sportEvents.containsKey(eventId)){
            throw new SportEventNotFoundException();
        }

        SportEvent s = getSportEvent(eventId);

        if (s.hasNoSubstitutes()){
            throw new NoSubstitutesException();
        }
        return s.getSubstitutes().values();        
    }

    @Override
    public void addAttender(String phone, String name, String eventId) throws AttenderAlreadyExistsException, SportEventNotFoundException, LimitExceededException {
        if (!sportEvents.containsKey(eventId)){
            throw new SportEventNotFoundException();
        }
        SportEvent s = getSportEvent(eventId);
        Attender a = new Attender(phone, name);

        if (s.getAttenderByPhone(phone) != null) {
            throw new AttenderAlreadyExistsException();
        }

        if (s.numAttenders() + s.numPlayers() < s.getMax()){
            s.addAttender(a);
        } else {
            throw new LimitExceededException();
        }
    }

    @Override
    public Attender getAttender(String phone, String sportEventId) throws SportEventNotFoundException, AttenderNotFoundException {
        if (!sportEvents.containsKey(sportEventId)){
            throw new SportEventNotFoundException();
        }

        SportEvent s = getSportEvent(sportEventId);

        Attender a = s.getAttenderByPhone(phone);
        if (a == null) {
            throw new AttenderNotFoundException();
        }
        return a;
    }

    @Override
    public Iterator<Attender> getAttenders(String eventId) throws SportEventNotFoundException, NoAttendersException {
        if (!sportEvents.containsKey(eventId)){
            throw new SportEventNotFoundException();
        }

        SportEvent s = getSportEvent(eventId);

        HashTable<String, Attender> attenders = s.getAttenders();

        if (attenders.isEmpty()){
            throw new NoAttendersException();
        }
        return attenders.values();
    }

    @Override
    public Iterator<OrganizingEntity> best5OrganizingEntities() throws NoAttendersException {
        Iterator<String> keys = this.organizingEntities.keys();
        int totalAttenders = 0;
        while (keys.hasNext()) {
            OrganizingEntity o = getOrganizingEntity(keys.next());
            totalAttenders += o.numAttenders();
            best5OrganizingEntities.update(o);            
        }
        if (totalAttenders == 0) {
            throw new NoAttendersException();
        }
        return best5OrganizingEntities.values();
    }

    @Override
    public SportEvent bestSportEventByAttenders() throws NoSportEventsException {
        Iterator<SportEvent> se_it = sportEvents.values();
        if (se_it == null){
            throw new NoSportEventsException();
        }

        SportEvent bestSportEvent = se_it.next();

        while (se_it.hasNext()){
            SportEvent bestSportEvent2 = se_it.next();
            if (bestSportEvent.compareTo(bestSportEvent2) > 0) {
                bestSportEvent = bestSportEvent2;
            }
        }
        return bestSportEvent;
    }

    @Override
    public void addFollower(String playerId, String playerFollowerId) throws PlayerNotFoundException {
        Player p = getPlayer(playerId);
        Player pf = getPlayer(playerFollowerId);
        if (p == null || pf == null) {
            throw new PlayerNotFoundException();
        }

        var v_p = this.followers.getVertex(p);
        v_p = v_p != null ? v_p : this.followers.newVertex(p);
        var v_pf = this.followers.getVertex(pf);
        v_pf = v_pf != null ? v_pf : this.followers.newVertex(pf);

        var e_p_pf = this.followers.getEdge(v_p, v_pf);
        if (e_p_pf == null) {
            this.followers.newEdge(v_p, v_pf);
        }
    }

    @Override
    public Iterator<Player> getFollowers(String playerId) throws PlayerNotFoundException, NoFollowersException {
        Player p = getPlayer(playerId);
        if (p == null) {
            throw new PlayerNotFoundException();
        }

        Vertex<Player> v_p = this.followers.getVertex(p);
        if (v_p == null) {
            throw new NoFollowersException();
        }

        Iterator<Edge<Player, Player>> it_e_p_pf = this.followers.edgesWithSource(v_p);
        if (!it_e_p_pf.hasNext()) {
            throw new NoFollowersException();
        }
        
        HashTable<String, Player> hash_pfs = new HashTable<String, Player>();
        Iterator<Vertex<Player>> it_pfs = this.followers.adjacencyList(v_p);

        while(it_pfs.hasNext()) {
            Vertex<Player> v_pf = it_pfs.next();
            if (this.followers.getEdge(v_p, v_pf) != null) {
                Player pf = v_pf.getValue();
                hash_pfs.put(pf.getId(), pf);
            }
        }        
        
        return hash_pfs.values();
    }

    @Override
    public Iterator<Player> getFollowings(String playerId) throws PlayerNotFoundException, NoFollowingException {
        Player pf = getPlayer(playerId);
        if (pf == null) {
            throw new PlayerNotFoundException();
        }

        Vertex<Player> v_pf = this.followers.getVertex(pf);
        if (v_pf == null) {
            throw new NoFollowingException();
        }

        Iterator<Edge<Player, Player>> it_e_p_pf = this.followers.edgedWithDestA(v_pf);
        if (!it_e_p_pf.hasNext()) {
            throw new NoFollowingException();
        }
        
        HashTable<String, Player> hash_ps = new HashTable<String, Player>();
        Iterator<Vertex<Player>> it_ps = this.followers.adjacencyList(v_pf);

        while(it_ps.hasNext()) {
            Vertex<Player> v_p = it_ps.next();
            if (this.followers.getEdge(v_p, v_pf) != null) {
                Player p = v_p.getValue();
                hash_ps.put(p.getId(), p);
            }
        }        
        
        return hash_ps.values();
    }

    @Override
    public Iterator<Player> recommendations(String playerId) throws PlayerNotFoundException, NoFollowersException {
        Iterator<Player> fs_of_p = getFollowers(playerId);
        LinkedList<Player> fs_of_fs_of_p = new LinkedList<Player>();
        
        while(fs_of_p.hasNext()) {
            Player f_of_p = fs_of_p.next();
            Iterator<Player> fs_of_f_of_p = null;

            try {
                fs_of_f_of_p = getFollowers(f_of_p.getId());
            }
            catch (NoFollowersException ex) { continue; }
            
            while(fs_of_f_of_p.hasNext()) {                
                Player f_of_f_of_p = fs_of_f_of_p.next();
                if (f_of_f_of_p.getId() == playerId) { continue; }

                boolean alreadyFollowing = false;
                Iterator<Player> it_followeds = getFollowers(playerId);
               
                while (!alreadyFollowing && it_followeds.hasNext()) {
                    Player followed = it_followeds.next();
                    if (followed.getId() == f_of_f_of_p.getId()) { alreadyFollowing = true;}
                }
                if (alreadyFollowing) { continue; }
                
                boolean alreadyInsert = false;
                var pos_fs_of_fs_of_p = fs_of_fs_of_p.positions();            
                while (!alreadyInsert && pos_fs_of_fs_of_p.hasNext()) {
                    Position<Player> next_fs_of_fs_of_p = pos_fs_of_fs_of_p.next();
                    if (next_fs_of_fs_of_p.getElem().getId() == f_of_f_of_p.getId()) {
                        alreadyInsert = true;                   
                    }
                }

                if (!alreadyInsert) {
                    fs_of_fs_of_p.insertEnd(f_of_f_of_p);
                }
            }
        }
        
        return fs_of_fs_of_p.values();        
    }

    @Override
    public Iterator<Post> getPosts(String playerId) throws PlayerNotFoundException, NoPostsException {
        Player p = getPlayer(playerId);
        if (p == null) {
           throw new PlayerNotFoundException();
        }
        
        Iterator<Player> it_following = null;
        
        try {
            it_following = getFollowings(playerId);
        }
        catch(NoFollowingException ex) { throw new NoPostsException(); }

        if (!it_following.hasNext()) {
            throw new NoPostsException();
        }

        LinkedList<Post> posts = new LinkedList<Post>();
        while(it_following.hasNext()) {
            Player next_followed = it_following.next();

            Iterator<Post> it_posts_by_followed = next_followed.getPosts();
            while(it_posts_by_followed.hasNext()) {
                Post next_post = it_posts_by_followed.next();
                posts.insertEnd(next_post);
            }
        }

        return posts.values();
    }

    @Override
    public int numPlayers() {
        return this.players.size();
    }

    @Override
    public int numOrganizingEntities() {
        return this.organizingEntities.size();
    }

    @Override
    public int numFiles() {
        return totalFiles;
    }

    @Override
    public int numRejectedFiles() {
        return this.rejectedFiles;
    }

    @Override
    public int numPendingFiles() {
        Iterator<File> file_it = this.files.values();
        int pendingFilesCount = 0;

        while(file_it.hasNext()) {
            File f = file_it.next();
            if (f.isPending()) {
                pendingFilesCount++;
            }
        }

        return pendingFilesCount;
    }

    @Override
    public int numSportEvents() {
        return this.sportEvents.size();
    }

    @Override
    public int numSportEventsByPlayer(String playerId) {
        Player player = getPlayer(playerId);
        return player != null ? player.numEvents() : 0;
    }

    @Override
    public int numPlayersBySportEvent(String sportEventId) {
        SportEvent sportEvent = getSportEvent(sportEventId);
        return sportEvent != null ? sportEvent.numPlayers() : 0;
    }

    @Override
    public int numSportEventsByOrganizingEntity(String orgId) {
        OrganizingEntity orEntity = getOrganizingEntity(orgId);
        return orEntity != null ? orEntity.numEvents() : 0;
    }

    @Override
    public int numSubstitutesBySportEvent(String sportEventId) {
        SportEvent sportEvent = getSportEvent(sportEventId);
        return sportEvent != null ? sportEvent.numSubstitutes() : 0;
    }

    @Override
    public Player getPlayer(String playerId) {
        return this.players.get(playerId);
    }

    @Override
    public SportEvent getSportEvent(String eventId) {
        return this.sportEvents.get(eventId);
    }

    @Override
    public OrganizingEntity getOrganizingEntity(String id) {
        return this.organizingEntities.get(id);
    }

    @Override
    public File currentFile() {
        return files.size() > 0 ? files.peek() : null;
    }

    @Override
    public int numRoles() {
        return this.numRoles;
    }

    @Override
    public Role getRole(String roleId) {
        for (Role r : this.roles) {
            if (r != null && r.getRoleId().equals(roleId)){
                return r;
            }
        }
        return null;
    }

    @Override
    public int numWorkers() {
        int workersCount = 0;
        for (Role r : this.roles) {
            if (r != null) {
                workersCount += r.numWorkers();
            }
        }
        return workersCount;
    }

    @Override
    public Worker getWorker(String dni) {
        for (Role r : this.roles) {
            if (r == null) { continue; }

            Worker w = r.getWorkerByDni(dni);
            if (w == null) { continue; }

            return w;            
        }
        return null;
    }

    @Override
    public int numWorkersByRole(String roleId) {
        Role role = getRole(roleId);
        return role != null ? role.numWorkers() : 0;
    }

    @Override
    public int numWorkersBySportEvent(String sportEventId) {
        SportEvent s = getSportEvent(sportEventId);
        return s.numWorkers();
    }

    @Override
    public int numRatings(String playerId) {
        Player p = getPlayer(playerId);
        return p.numRatings();
    }

    @Override
    public int numAttenders(String sportEventId) {
        SportEvent s = getSportEvent(sportEventId);
        if (s == null){
            return 0;
        }
        return s.numAttenders();
    }

    @Override
    public int numFollowers(String playerId) {
        int followersCount = 0;
        Player p = getPlayer(playerId);
        Vertex<Player> v_p = this.followers.getVertex(p);
        Iterator<Edge<Player, Player>> it_e_p_pf = this.followers.edgesWithSource(v_p);
        
        while(it_e_p_pf.hasNext()) {
            it_e_p_pf.next();
            followersCount++;
        }

        return followersCount;
    }

    @Override
    public int numFollowings(String playerId) {
        int followingsCount = 0;
        Player pf = getPlayer(playerId);
        Vertex<Player> v_pf = this.followers.getVertex(pf);
        Iterator<Edge<Player, Player>> it_e_p_pf = this.followers.edgedWithDestA(v_pf);
        
        while(it_e_p_pf.hasNext()) {
            it_e_p_pf.next();
            followingsCount++;
        }

        return followingsCount;
    }    

    private void updateMostActivePlayer(Player player) {
        if (mostActivePlayer == null) {
            mostActivePlayer = player;
        }
        else if (player.numSportEvents() > mostActivePlayer.numSportEvents()) {
            mostActivePlayer = player;
        }
    }
    
    private void updateBestSportEvent(SportEvent sportEvent) {
        bestSportEvent.delete(sportEvent);
        bestSportEvent.update(sportEvent);
    }
}
