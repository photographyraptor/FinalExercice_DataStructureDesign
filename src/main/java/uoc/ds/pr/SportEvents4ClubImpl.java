package uoc.ds.pr;

import java.time.LocalDate;
import edu.uoc.ds.adt.nonlinear.DictionaryAVLImpl;
import edu.uoc.ds.adt.nonlinear.HashTable;
import edu.uoc.ds.adt.nonlinear.PriorityQueue;
import edu.uoc.ds.adt.sequential.LinkedList;
import edu.uoc.ds.traversal.Iterator;
import uoc.ds.pr.exceptions.*;
import uoc.ds.pr.model.*;
import uoc.ds.pr.util.OrderedVector;

public class SportEvents4ClubImpl implements SportEvents4Club {
    private DictionaryAVLImpl<String, Player> players;
    private Player mostActivePlayer;
    private HashTable<String, OrganizingEntity> organizingEntities;
    private PriorityQueue<File> files;
    private DictionaryAVLImpl<String, SportEvent> sportEvents;
    private OrderedVector<SportEvent> bestSportEvent;
    //private LinkedList<Worker> workers;
    private Role[] roles;
    private int numRoles;
    
    private int totalFiles;
    private int rejectedFiles;

    public SportEvents4ClubImpl() {
        this.players = new DictionaryAVLImpl<String, Player>();
        this.mostActivePlayer = null;
        this.organizingEntities = new HashTable<String, OrganizingEntity>();
        this.files = new PriorityQueue<>();
        this.sportEvents = new DictionaryAVLImpl<String, SportEvent>();
        this.bestSportEvent = new OrderedVector<SportEvent>(MAX_NUM_SPORT_EVENTS, SportEvent.CMP_V);
        //this.workers = new LinkedList<Worker>();
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
            r.setDescription(description); //TODO: mirar si actualiza item del array
        } else {
            r = new Role(roleId, description);
            roles[numRoles++] = r;
        }
    }

    @Override
    public void addWorker(String dni, String name, String surname, LocalDate birthDay, String roleId) {
        Role r = getRole(roleId);
        Worker w = r.getWorkerByDni(dni);
        
        if (w == null) {
            // add to r
        }
        else {
            // update w in r
        }
    }
//TODO me quedé aqui
    @Override
    public void assignWorker(String dni, String eventId) throws WorkerNotFoundException, WorkerAlreadyAssignedException, SportEventNotFoundException {

    }

    @Override
    public Iterator<Worker> getWorkersBySportEvent(String eventId) throws SportEventNotFoundException, NoWorkersException {
        return null;
    }

    @Override
    public Iterator<Worker> getWorkersByRole(String roleId) throws NoWorkersException {
        return null;
    }

    @Override
    public Level getLevel(String playerId) throws PlayerNotFoundException {
        return null;
    }

    @Override
    public Iterator<Enrollment> getSubstitutes(String eventId) throws SportEventNotFoundException, NoSubstitutesException {
        return null;
    }

    @Override
    public void addAttender(String phone, String name, String eventId) throws AttenderAlreadyExistsException, SportEventNotFoundException, LimitExceededException {

    }

    @Override
    public Attender getAttender(String phone, String sportEventId) throws SportEventNotFoundException, AttenderNotFoundException {
        return null;
    }

    @Override
    public Iterator<Attender> getAttenders(String eventId) throws SportEventNotFoundException, NoAttendersException {
        return null;
    }

    @Override
    public Iterator<OrganizingEntity> best5OrganizingEntities() throws NoAttendersException {
        return null;
    }

    @Override
    public SportEvent bestSportEventByAttenders() throws NoSportEventsException {
        return null;
    }

    @Override
    public void addFollower(String playerId, String playerFollowerId) throws PlayerNotFoundException {

    }

    @Override
    public Iterator<Player> getFollowers(String playerId) throws PlayerNotFoundException, NoFollowersException {
        return null;
    }

    @Override
    public Iterator<Player> getFollowings(String playerId) throws PlayerNotFoundException, NoFollowingException {
        return null;
    }

    @Override
    public Iterator<Player> recommendations(String playerId) throws PlayerNotFoundException, NoFollowersException {
        return null;
    }

    @Override
    public Iterator<Post> getPosts(String playerId) throws PlayerNotFoundException, NoPostsException {
        return null;
    }

    @Override
    public int numPlayers() {
        return 0;
    }

    @Override
    public int numOrganizingEntities() {
        return 0;
    }

    @Override
    public int numFiles() {
        return 0;
    }

    @Override
    public int numRejectedFiles() {
        return 0;
    }

    @Override
    public int numPendingFiles() {
        return 0;
    }

    @Override
    public int numSportEvents() {
        return 0;
    }

    @Override
    public int numSportEventsByPlayer(String playerId) {
        return 0;
    }

    @Override
    public int numPlayersBySportEvent(String sportEventId) {
        return 0;
    }

    @Override
    public int numSportEventsByOrganizingEntity(String orgId) {
        return 0;
    }

    @Override
    public int numSubstitutesBySportEvent(String sportEventId) {
        return 0;
    }

    @Override
    public Player getPlayer(String playerId) {
        return null;
    }

    @Override
    public SportEvent getSportEvent(String eventId) {
        return null;
    }

    @Override
    public OrganizingEntity getOrganizingEntity(String id) {
        return null;
    }

    @Override
    public File currentFile() {
        return null;
    }

    @Override
    public int numRoles() {
        return 0;
    }

    @Override
    public Role getRole(String roleId) {
        return null;
    }

    @Override
    public int numWorkers() {
        return 0;
    }

    @Override
    public Worker getWorker(String dni) {
        return null;
    }

    @Override
    public int numWorkersByRole(String roleId) {
        return 0;
    }

    @Override
    public int numWorkersBySportEvent(String sportEventId) {
        return 0;
    }

    @Override
    public int numRatings(String playerId) {
        return 0;
    }

    @Override
    public int numAttenders(String sportEventId) {
        return 0;
    }

    @Override
    public int numFollowers(String playerId) {
        return 0;
    }

    @Override
    public int numFollowings(String playerId) {
        return 0;
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
