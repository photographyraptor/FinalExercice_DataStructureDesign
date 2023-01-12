package uoc.ds.pr.model;

import edu.uoc.ds.adt.helpers.Position;
import edu.uoc.ds.adt.sequential.LinkedList;
import edu.uoc.ds.adt.sequential.List;
import edu.uoc.ds.traversal.Iterator;

public class Role {
    private String roleId;
    private String description;
    private LinkedList<Worker> workers;

    public Role(String roleId, String description) {
        this.roleId = roleId;
        this.description = description;
        this.workers = new LinkedList<Worker>();
    }

    public String getRoleId() {
        return this.roleId;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public void updateWorker(Role oldRole, Worker newWorker) {
        var pos_workers = oldRole.getWorkers().positions();            
        while (pos_workers.hasNext()) {
            Position<Worker> next_worker = pos_workers.next();
            if (next_worker.getElem().getDni() == newWorker.getDni()) {
                oldRole.getWorkers().delete(next_worker);
                this.workers.insertEnd(newWorker);
                break;
            }
        }
    }
}
