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

    public void putWorker(Worker w) {
        var worker_it = getWorkers().values();
        boolean updated = false;
        
        while (worker_it.hasNext() && !updated) {
            var nextWorker = worker_it.next();
            if (nextWorker.getDni() == w.getDni()) {
                nextWorker = w; //TODO: revisar si actualiza bien
                updated = true;
            }
        }

        if (!updated) {
            this.workers.insertEnd(w);
        }
    }
}
