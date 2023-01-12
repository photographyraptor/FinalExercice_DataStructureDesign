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

    private LinkedList<Worker> workers;
    private Queue<Enrollment> enrollments;
    private PriorityQueue<Enrollment> substitutues;
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
        this.enrollments = new QueueArrayImpl<Enrollment>(MAX_NUM_ENROLLMENT);
        this.substitutues = new PriorityQueue<Enrollment>(MAX_NUM_ENROLLMENT);
        this.ratings = new LinkedList<Rating>();
        this.workers = new LinkedList<Worker>();
        this.attenders = new HashTable<String, Attender>();
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
        return this.file;
    }

    public OrganizingEntity getOrganizingEntity() {
        File f = getFile();
        return f.getOrganizingEntity();
    }

    public void setFile(File file) {
        this.file = file;
    }


    public double rating() {
        return (this.ratings.size()>0?(sumRating / this.ratings.size()):0);
    }

    public void addRating(SportEvents4Club.Rating rating, String message, Player player) {
        Rating newRating = new Rating(rating, message, player);
        ratings.insertEnd(newRating);
        sumRating+=rating.getValue();
        player.addRating(newRating);
    }

    public boolean hasRatings() {
        return ratings.size()>0;
    }

    public Iterator<Rating> ratings() {
        return ratings.values();
    }

    public void addEnrollment(Player player) {
        enrollments.add(new Enrollment(player, false));
    }

    public void addSubstitute(Player player) {
        substitutues.add(new Enrollment(player, true));
    }

    public boolean is(String eventId) {
        return this.eventId.equals(eventId);
    }

    @Override
    public int compareTo(SportEvent se2) {
        return this.getEventId().compareTo(se2.getEventId());
        //return Double.compare(rating(), se2.rating());
    }

    public boolean isFull() {
        return (enrollments.size()>=max);
    }

    public boolean hasNoSubstitutes() {
        return (substitutues.size() == 0);
    }

    public int numPlayers() {
        return enrollments.size() + substitutues.size();
    }
    
    public int numAttenders() {
        return this.attenders.size();
    }

    public int numSubstitutes() {
        return this.substitutues.size();
    }

    public HashTable<String, Attender> getAttenders() {
        return this.attenders;
    }

    public Attender getAttenderByPhone(String phone) {
        return this.attenders.get(phone);
    }

    public void addAttender(Attender attender) {
        this.attenders.put(attender.getPhone(), attender);
    }
    
    public LinkedList<Worker> getWorkers() {
        return this.workers;
    }

    public int numWorkers() {
        return this.workers.size();
    }

    public Worker getWorkerByDni(String dni) {
        var worker_it = getWorkers().values();
        
        while (worker_it.hasNext()) {
            var nextWorker = worker_it.next();
            if (nextWorker.getDni() == dni) {
                return nextWorker;
            }
        }
        return null;
    }
    
    public void addWorker(Worker w) {
        this.workers.insertEnd(w);
    }

    public Queue<Enrollment> getEnrollments() {
        return this.enrollments;
    }

    public Queue<Enrollment> getSubstitutes() {
        return this.substitutues;
    }
}
