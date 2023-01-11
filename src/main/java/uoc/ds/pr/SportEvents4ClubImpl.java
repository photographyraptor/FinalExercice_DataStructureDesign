package uoc.ds.pr;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;

import edu.uoc.ds.adt.nonlinear.AVLTree;
import edu.uoc.ds.adt.nonlinear.DictionaryAVLImpl;
import edu.uoc.ds.adt.nonlinear.HashTable;
import edu.uoc.ds.adt.nonlinear.PriorityQueue;
import edu.uoc.ds.traversal.Iterator;

import edu.uoc.ds.traversal.IteratorTraversalKeysImpl;
import uoc.ds.pr.model.*;
import uoc.ds.pr.exceptions.*;
import uoc.ds.pr.util.*;

import static java.lang.Boolean.FALSE;

public class SportEvents4ClubImpl implements SportEvents4Club {

    private DictionaryAVLImpl<String, Player> players;
    private int numPlayers;

    private HashTable<String, OrganizingEntity> organizingEntities;
    private int numOrganizingEntities;

    private PriorityQueue<File> files;
    private int totalFiles;
    private DictionaryAVLImpl<String, SportEvent> sportEvents;
    private int rejectedFiles;
    private Player mostActivePlayer;
    private OrderedVector<SportEvent> bestSportEvent;
    private OrderedVector<OrganizingEntity> best5OrganizingEntities;
    private Role[] roles;
    private int numRoles;

    private HashTable<String, Worker> workers;
    private int numWorkers;

    public SportEvents4ClubImpl() {
        players = new DictionaryAVLImpl<>();
        numPlayers = 0;
        organizingEntities = new HashTable<>();
        numOrganizingEntities = 0;
        files = new PriorityQueue<>();
        sportEvents = new DictionaryAVLImpl<>();
        totalFiles = 0;
        rejectedFiles = 0;
        mostActivePlayer = null;
        bestSportEvent = new OrderedVector<SportEvent>(MAX_NUM_SPORT_EVENTS, SportEvent.CMP_V);
        best5OrganizingEntities = new OrderedVector<OrganizingEntity>(MAX_ORGANIZING_ENTITIES_WITH_MORE_ATTENDERS, OrganizingEntity.CMP_O);
        roles = new Role[MAX_ROLES];
        numRoles = 0;
        workers = new HashTable<>();
        numWorkers = 0;

    }
    @Override
    public void addPlayer(String playerId, String name, String surname, LocalDate birthday) {
        Player p = new Player(playerId, name, surname, birthday);
        if (!players.containsKey(playerId)) {
            numPlayers++;
        }
        players.put(playerId, p);
    }

    public Player getPlayer(String playerId) { return players.get(playerId); }

    @Override
    public void addOrganizingEntity(String organizationId, String name, String description) {
        OrganizingEntity o = new OrganizingEntity(organizationId, name, description);
        if (!organizingEntities.containsKey(organizationId)) {
            numOrganizingEntities++;
        } else {
            organizingEntities.delete(organizationId);
        }
        organizingEntities.put(organizationId, o);
    }

    @Override
    public void addFile(String id, String eventId, String orgId, String description, Type type, byte resources, int max, LocalDate startDate, LocalDate endDate) throws OrganizingEntityNotFoundException {
        OrganizingEntity o = getOrganizingEntity(orgId);
        if (o == null){
            throw new OrganizingEntityNotFoundException();
        }
        files.add(new File(id, eventId, description, type, startDate, endDate, resources, max, o));
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

        player.addEvent(sportEvent);
        if (!sportEvent.isFull()) {
            sportEvent.addEnrollment(player);
        }
        else {
            sportEvent.addEnrollmentAsSubstitute(player);
            throw new LimitExceededException();
        }
        updateMostActivePlayer(player);
    }

    public File currentFile() {
        return (files.size() > 0 ? files.peek() : null);
    }

    @Override
    public double getRejectedFiles() {
        return (double) rejectedFiles / totalFiles;
    }

    @Override
    public Iterator<SportEvent> getSportEventsByOrganizingEntity(String organizationId) throws NoSportEventsException {
        OrganizingEntity organizingEntity = getOrganizingEntity(organizationId);

        if (organizingEntity==null || !organizingEntity.hasActivities()) {
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
        if (player==null || !player.hasEvents()) {
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

        sportEvent.addRating(rating, message, player);
        updateBestSportEvent(sportEvent);
    }

    private void updateBestSportEvent(SportEvent sportEvent) {
        bestSportEvent.delete(sportEvent);
        bestSportEvent.update(sportEvent);
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

    private void updateMostActivePlayer(Player player) {
        if (mostActivePlayer == null) {
            mostActivePlayer = player;
        }
        else if (player.numSportEvents() > mostActivePlayer.numSportEvents()) {
            mostActivePlayer = player;
        }
    }

    @Override
    public Player mostActivePlayer() throws PlayerNotFoundException {
        if (mostActivePlayer == null) {
            throw new PlayerNotFoundException();
        }

        return mostActivePlayer;
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
            r.roleId = roleId;
            r.description = description;
        } else {
            r = new Role(roleId, description);
            roles[numRoles++] = r;
        }
    }

    @Override
    public void addWorker(String dni, String name, String surname, LocalDate birthDay, String roleId) {
        Worker w = new Worker(dni, name, surname, birthDay, roleId);
        Role r = getRole(roleId);

        if (!workers.containsKey(w.getDni())) {
            numWorkers++;
            r.addWorker(w);
        } else {
            //
            workers.delete(w.getDni());
            /*
            // se tiene que eliminar el worker de la lista de workers en el viejo rol
            // se tiene que añadir el worker en la lista de workers del nuevo rol
            if (w.getRoleId() != roleId) {
                Role old_role = getRole(w.getRoleId());
                old_role.deleteWorker(w);
                r.addWorker(w);
            }*/
        }
        workers.put(dni, w);
    }
    @Override
    public void assignWorker(String dni, String eventId) throws WorkerNotFoundException, WorkerAlreadyAssignedException, SportEventNotFoundException {
        if (!sportEvents.containsKey(eventId)){
            throw new SportEventNotFoundException();
        }

        if (!workers.containsKey(dni)){
            throw new WorkerNotFoundException();
        }

        SportEvent s = sportEvents.get(eventId);

        if (s.isInWorkers(dni)){
            throw new WorkerAlreadyAssignedException();
        } else {
            Worker w = workers.get(dni);
            s.addWorker(w);
        }
    }

    @Override
    public Iterator<Worker> getWorkersBySportEvent(String eventId) throws SportEventNotFoundException, NoWorkersException {
        if (!sportEvents.containsKey(eventId)){
            throw new SportEventNotFoundException();
        }
        SportEvent s = sportEvents.get(eventId);
        if (s.noWorkers()){
            throw new NoWorkersException();
        } else {
            return s.getWorkers();
        }
    }
    @Override
    public Iterator<Worker> getWorkersByRole(String roleId) throws NoWorkersException {
        Role r = getRole(roleId);
        if (r.workers.isEmpty()){
            throw new NoWorkersException();
        } else {
            return r.workers.values();
        }
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

        if (s.noSubstitutes()){
            throw new NoSubstitutesException();
        } else {
            return s.getSubstitutes();
        }
    }

    @Override
    public void addAttender(String phone, String name, String eventId) throws AttenderAlreadyExistsException, SportEventNotFoundException, LimitExceededException {
        if (!sportEvents.containsKey(eventId)){
            throw new SportEventNotFoundException();
        }
        SportEvent s = getSportEvent(eventId);
        Attender a = new Attender(phone, name, eventId);

        if (s.isInAttendees(phone)){
            throw new AttenderAlreadyExistsException();
        }

        if (s.numAttenders() < s.getMax()){
            s.addAttender(phone, a);
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

        if (s.isInAttendees(phone)){
            return s.getAttender(phone);
        } else {
            throw new AttenderNotFoundException();
        }
    }
    @Override
    public Iterator<Attender> getAttenders(String eventId) throws SportEventNotFoundException, NoAttendersException {
        if (!sportEvents.containsKey(eventId)){
            throw new SportEventNotFoundException();
        }
        SportEvent s = getSportEvent(eventId);

        if (s.noAttenders()){
            throw new NoAttendersException();
        } else {
            return s.getAttenders();
        }
    }
    @Override
    public Iterator<OrganizingEntity> best5OrganizingEntities() throws NoAttendersException {

        Iterator<String> keys = organizingEntities.keys();
        while (keys.hasNext()) {
            OrganizingEntity o = getOrganizingEntity(keys.next());

            if (o.numAttenders() == 0){
                throw new NoAttendersException();
            } else {
                best5OrganizingEntities.update(o);
            }
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
            if (bestSportEvent.numAttenders() < bestSportEvent2.numAttenders()){
                bestSportEvent = bestSportEvent2;
            }
        }
        return bestSportEvent;
    }

    @Override
    public void addFollower(String playerId, String playerFollowerId) throws PlayerNotFoundException {
        throw new PlayerNotFoundException();
    }

    @Override
    public Iterator<Player> getFollowers(String playerId) throws PlayerNotFoundException, NoFollowersException {
        if (1 == 0){
            throw new PlayerNotFoundException();
        } else {
            throw new NoFollowersException();
        }
    }

    @Override
    public Iterator<Player> getFollowings(String playerId) throws PlayerNotFoundException, NoFollowingException {
        if (1 == 0){
            throw new PlayerNotFoundException();
        } else {
            throw new NoFollowingException();
        }
    }

    @Override
    public Iterator<Player> recommendations(String playerId) throws PlayerNotFoundException, NoFollowersException {
        if (1 == 0){
            throw new PlayerNotFoundException();
        } else {
            throw new NoFollowersException();
        }
    }

    @Override
    public Iterator<Post> getPosts(String playerId) throws PlayerNotFoundException, NoPostsException {
        if (1 == 0){
            throw new PlayerNotFoundException();
        } else {
            throw new NoPostsException();
        }
    }

    @Override
    public int numPlayers() { return numPlayers;}
    @Override
    public int numOrganizingEntities() { return organizingEntities.size(); }
    @Override
    public int numFiles() { return totalFiles; }
    @Override
    public int numRejectedFiles() { return rejectedFiles; }
    @Override
    public int numPendingFiles() { return files.size(); }
    @Override
    public int numSportEvents() { return sportEvents.size(); }
    @Override
    public int numSportEventsByPlayer(String playerId) {
        Player player = getPlayer(playerId);
        return (player!=null?player.numEvents():0);
    }
    @Override
    public int numPlayersBySportEvent(String sportEventId) {
        SportEvent sportEvent = getSportEvent(sportEventId);
        return (sportEvent!=null?sportEvent.numPlayers(): 0);
    }
    @Override
    public int numSportEventsByOrganizingEntity(String orgId) {
        OrganizingEntity o = getOrganizingEntity(orgId);
        return o != null ? o.numEvents() : 0;
    }
    @Override
    public int numSubstitutesBySportEvent(String sportEventId) {
        SportEvent sportEvent = getSportEvent(sportEventId);
        return (sportEvent!=null?sportEvent.getNumSubstitutes():0);
    }
    @Override
    public SportEvent getSportEvent(String eventId) { return sportEvents.get(eventId); }
    @Override
    public OrganizingEntity getOrganizingEntity(String id) { return organizingEntities.get(id); }
    @Override
    public int numRoles() { return numRoles; }
    @Override
    public Role getRole(String roleId) {
        for (Role r : roles) {
            if (r == null) {
                return null;
            } else if (r.roleId.equals(roleId)){
                return r;
            }
        }
        return null;
    }
    @Override
    public int numWorkers() { return numWorkers; }
    @Override
    public Worker getWorker(String dni) {
        Worker w = workers.get(dni);
        return w;
    }
    @Override
    public int numWorkersByRole(String roleId) {
        Role r = getRole(roleId);
        return r.numWorkers();
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
        return 0;
    }

    @Override
    public int numFollowings(String playerId) {
        return 0;
    }
}
