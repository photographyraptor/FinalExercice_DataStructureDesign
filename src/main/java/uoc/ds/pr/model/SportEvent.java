package uoc.ds.pr.model;

import edu.uoc.ds.adt.nonlinear.HashTable;
import edu.uoc.ds.adt.nonlinear.PriorityQueue;
import edu.uoc.ds.adt.sequential.LinkedList;
import edu.uoc.ds.adt.sequential.List;
import edu.uoc.ds.adt.sequential.Queue;
import edu.uoc.ds.adt.sequential.QueueArrayImpl;
import edu.uoc.ds.traversal.Iterator;
import uoc.ds.pr.SportEvents4Club;

import java.time.LocalDate;
import java.util.Comparator;


import static uoc.ds.pr.SportEvents4Club.MAX_NUM_ENROLLMENT;

public class SportEvent implements Comparable<SportEvent> {
    public static final Comparator<SportEvent> CMP_V = (se1, se2)->Double.compare(se1.rating(), se2.rating());
    public static final Comparator<String> CMP_K = (k1, k2)-> k1.compareTo(k2);

    private String eventId;
    private String description;
    private SportEvents4Club.Type type;
    private LocalDate startDate;
    private LocalDate endDate;
    private int max;

    private File file;

    private List<Rating> ratings;
    private double sumRating;

    private int numSubstitutes;

    private Queue<Enrollment> enrollments;

    private PriorityQueue<Enrollment> substitutes;

    private List<Worker> workers;

    private HashTable<String, Attender> attenders;


    public SportEvent(String eventId, String description, SportEvents4Club.Type type,
                      LocalDate startDate, LocalDate endDate, int max, File file) {
        setEventId(eventId);
        setDescription(description);
        setStartDate(startDate);
        setEndDate(endDate);
        setType(type);
        setMax(max);
        setFile(file);
        this.enrollments = new QueueArrayImpl<>(MAX_NUM_ENROLLMENT);
        this.ratings = new LinkedList<>();
        numSubstitutes = 0;
        this.substitutes = new PriorityQueue<>(MAX_NUM_ENROLLMENT);
        this.workers = new LinkedList<>();
        this.attenders = new HashTable<>();

    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SportEvents4Club.Type getType() {
        return type;
    }

    public void setType(SportEvents4Club.Type type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }


    public double rating() {
        return (this.ratings.size()>0?(sumRating / this.ratings.size()):0);
    }

    public void addRating(SportEvents4Club.Rating rating, String message, Player player) {
        Rating newRating = new Rating(rating, message, player);
        player.addRating(newRating);
        ratings.insertEnd(newRating);
        sumRating+=rating.getValue();
    }

    public boolean hasRatings() {
        return ratings.size()>0;
    }

    public Iterator<Rating> ratings() {
        return ratings.values();
    }


    public void addEnrollment(Player player) {
        addEnrollment(player, false);
    }

    public void addEnrollment(Player player, boolean isSubstitute) {
        enrollments.add(new Enrollment(player, isSubstitute));
    }

    public void addSubstitutes(Player player, boolean isSubstitute) {
        substitutes.add(new Enrollment(player, isSubstitute));
    }

    public boolean is(String eventId) {
        return this.eventId.equals(eventId);
    }

    @Override
    public int compareTo(SportEvent se2) {
        return Double.compare(rating(), se2.rating() );
    }

    public boolean isFull() {
        return (enrollments.size()>=max);
    }

    public int numPlayers() {
        return enrollments.size();
    }

    public void incSubstitutes() {
        numSubstitutes++;
    }

    public void addEnrollmentAsSubstitute(Player player) {
        addEnrollment(player, true);
        //addSubstitutes(player, true); //TODO decidir que hacer con sustitutos
        incSubstitutes();
    }

    public int getNumSubstitutes() {
        return numSubstitutes;
    }
    public OrganizingEntity getOrganizingEntity() { return null; }

    public boolean noSubstitutes() {
        return substitutes.isEmpty();
        /*
        Iterator<Enrollment> it_enr = enrollments.values();
        while (it_enr.hasNext()) {
            Enrollment e = it_enr.next();
            if (e.isSubtitute) {
                return false;
            }
        }
        return true;*/
    }
    public Iterator<Enrollment> getSubstitutes() { return substitutes.values(); }
    public void addWorker(Worker worker) {
        workers.insertEnd(worker);
    }
    public boolean noWorkers() { return workers.isEmpty(); }
    public Iterator<Worker> getWorkers() {
        return workers.values();
    }

    public int numWorkers() { return workers.size(); }
    public boolean isInWorkers(String dni) {
        boolean found = false;
        Worker worker = null;
        Iterator<Worker> it = this.workers.values();
        while (it.hasNext() && !found) {
            worker = it.next();
            found = worker.getDni().equals(dni);
        }
        return found;
    }

    public boolean isInAttendees(String phone){
        return attenders.containsKey(phone);
    }

    public int numAttenders() { return attenders.size(); }

    public void addAttender(String phone, Attender attender) { attenders.put(phone, attender);}

    public Attender getAttender(String phone){ return attenders.get(phone); }

    public Iterator<Attender> getAttenders() { return attenders.values(); }

    public boolean noAttenders() { return attenders.isEmpty(); }

}
