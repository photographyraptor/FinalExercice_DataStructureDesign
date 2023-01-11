package uoc.ds.pr.model;

import edu.uoc.ds.adt.helpers.Position;
import edu.uoc.ds.adt.sequential.LinkedList;
import edu.uoc.ds.adt.sequential.List;
import edu.uoc.ds.traversal.Iterator;
import java.time.LocalDate;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Role {
    public String roleId;
    public String description;
    public List<Worker> workers;

    public Role(String roleId, String description) {
        this.setRoleId(roleId);
        this.setDescription(description);
        this.workers = new LinkedList<>();
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleId() {
        return roleId;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    public boolean is(String roleId) {
        return roleId.equals(roleId);
    }

    public void addWorker(Worker worker) {
        if (!isInWorkers(worker.getDni())) {
            workers.insertEnd(worker);
        }
    }

    public void deleteWorker(String dni) {
    }

    public int numWorkers() {
        return workers.size();
    }

    private boolean isInWorkers(String dni) {
        Iterator<Worker> it_w = workers.values();
        while (it_w.hasNext()) {
            var w = it_w.next();
            if (w.getDni().equals(dni)){
                return TRUE;
            }
        }
        return FALSE;
    }

    public Iterator<Worker> getWorkers() {
        return workers.values();
    }

    public boolean hasWorkers() {
        return this.workers.size()>0;
    }

}
