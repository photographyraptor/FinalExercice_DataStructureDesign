package uoc.ds.pr.model;

import java.util.Comparator;

import edu.uoc.ds.adt.sequential.LinkedList;
import edu.uoc.ds.adt.sequential.List;
import edu.uoc.ds.traversal.Iterator;

public class OrganizingEntity implements Comparable<OrganizingEntity> {
    public static final Comparator<OrganizingEntity> CMP_O = (org1, org2)->Integer.compare(org1.numAttenders(), org2.numAttenders());
    
    private String organizationId;
    private String description;
    private String name;
    private List<SportEvent> events;

    public OrganizingEntity(String organizationId, String name, String description) {
        this.organizationId = organizationId;
        this.name = name;
        this.description = description;
        events = new LinkedList<>();
    }
    
    @Override
    public int compareTo(OrganizingEntity o) {
        return Double.compare(numAttenders(), o.numAttenders());
    }

    public String getName() {
        return name;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public String getDescription() {
        return description;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Iterator<SportEvent> activities() {
        return events.values();
    }

    public void addEvent(SportEvent sportEvent) {
        events.insertEnd(sportEvent);
    }

    public int numEvents() {
        return events.size();
    }

    public int numAttenders() {
        int totalAttendants = 0;
        Iterator<SportEvent> se_it = this.events.values();

        while (se_it.hasNext()) {
            SportEvent se = se_it.next();
            totalAttendants += se.numAttenders();
        }
        return totalAttendants;
    }

    public boolean hasActivities() {
        return events.size() > 0;
    }

    public Iterator<SportEvent> sportEvents() {
        return events.values();
    }
}
