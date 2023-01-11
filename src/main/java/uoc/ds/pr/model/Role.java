package uoc.ds.pr.model;

import edu.uoc.ds.adt.sequential.LinkedList;

public class Role {
    private String roleId;
    private String description;
    private LinkedList<Worker> workers;

    public Role(String roleId, String description) {
        this.roleId = roleId;
        this.description = description;
        this.workers = new LinkedList<Worker>();
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
}
