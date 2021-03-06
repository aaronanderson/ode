package org.apache.ode.bpel.rapi;


/**
 * Engine-provided context for process lifecycle management. 
 * 
 * @author mszefler
 *
 */
public interface ProcessControlContext {

    void forceFlush();

    void forceRollback();
    
    /**
     * Should be invoked by process template, signalling process completion with
     * no faults.
     * 
     */
    void completedOk();

    /**
     * Should be invoked by process template, signalling process completion with
     * fault.
     */
    void completedFault(FaultInfo faultData);

    /**
     * Terminates the process / sets state flag to terminate and ceases all
     * processing for the instance.
     */
    void terminate();

}
