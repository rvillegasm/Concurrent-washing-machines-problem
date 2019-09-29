package co.edu.eafit.dis.st0257.concurrencia.lavdual;

public class AgenteVoid implements Sincronizador {

    private GenCargas genCargas;
    private int cargaSimultanea;
    private boolean llegadaA, llegadaB;
    private boolean cedeA, cedeB;
    private boolean llegadaFinalA, llegadaFinalB;
    private boolean finalA, finalB;
    
    public AgenteVoid(GenCargas genCargas) {
        this.genCargas = genCargas;
        cargaSimultanea = 0;
        llegadaA = false;
        llegadaB = false;
        cedeA = false;
        cedeB = false;
        llegadaA = false;
        llegadaB = false;
        finalA = false;
        finalB = false;
    }
    /**
     * arracar - se encarga de iniciar la ejecución de una
     *           lavadora.
     * @param lavadoraID determina quien pone una carga
     * @param cap        determina la capacidad inicial de la lavadora.
     */
    public synchronized void arrancar(LavadoraID lavadoraID, int cap) {
        contribuirCargaSimultanea(cap); // seccion critica para aportar la carga
        if (lavadoraID == LavadoraID.LavadoraA) { 
            llegadaFinalB = false; // Se reinician las variables de condicion de B
            finalB = false;
            llegadaA = true; // A avisa que llega
            while (!llegadaB) {
                try{
                    wait(); // A espera a que B llegue
                } catch (InterruptedException ie) { }
            }
            notifyAll(); // A Despierta a B
        }
        else {
            llegadaFinalA = false; // Se reinician las variables de condicion de B
            finalA = false;
            llegadaB = true;
            while (!llegadaA) {
                try {
                    wait();
                } catch (InterruptedException ie) { }
            }
            notifyAll(); // B Despierta A
        }
        // ----------------------------------------------------------- Barrera
        if (cargaSimultanea > this.genCargas.obtenerCargaMax()) {
            cederElTurno(lavadoraID);
            // lava...
        }
        // Si no cumple la cond, lavan al mismo tiempo
    }


    /**
     * parar - se encarga de parar la ejecución de una
     *         lavadora.
     * @param lavadoraID determina quien pone un carga.
     */
    public synchronized void parar(LavadoraID lavadoraID) {
        if (cargaSimultanea > this.genCargas.obtenerCargaMax()) {
            if (lavadoraID == LavadoraID.LavadoraA) {
                cedeA = true; // A cede el turno porque ya lavo
                notifyAll();
            }
            else {
                cedeB = true; // B cede el turno porque ya lavo
                notifyAll();
            }
        }
        // En ambos casos...
        if (lavadoraID == LavadoraID.LavadoraA) {
            llegadaFinalA = true; // A avisa que llega
            while (!llegadaFinalB) { 
                try {
                    wait(); // A espera por B
                } catch (InterruptedException ie) { }
            }
            notifyAll();
        }
        else {
            llegadaFinalB = true; // B avisa que llega
            while (!llegadaFinalA) {
                try {
                    wait(); // B espera por A
                } catch (InterruptedException ie) { }
            }
            notifyAll();
        }
        // ----------------------------------------------------------- Barrera        
        restaurarVariables(lavadoraID);
        if (lavadoraID == LavadoraID.LavadoraA) {
            finalA = true; // A avisa que llega
            while (!finalB) { 
                try {
                    wait(); // A espera por B
                } catch (InterruptedException ie) { }
            }
            notifyAll();
        }
        else {
            finalB = true; // B avisa que llega
            while (!finalA) {
                try {
                    wait(); // B espera por A
                } catch (InterruptedException ie) { }
            }
            notifyAll();
        }
        // ----------------------------------------------------------- Barrera
    }

    private synchronized void contribuirCargaSimultanea(int cap) {
        cargaSimultanea += cap;
    }

    private synchronized void cederElTurno(LavadoraID lavadoraID) {
        if (lavadoraID == LavadoraID.LavadoraA) {
            while (!cedeB) { // Si B no me ha cedido el turno, yo se lo cedo
                cedeA = true;
                try {
                    wait();
                } catch (InterruptedException ie) { }
            }
            // lava...
        }
        else {
            while (!cedeA) { // Si A no me ha cedido el turno, yo se lo cedo
                cedeB = true;
                try {
                    wait();
                } catch (InterruptedException ie) { }
            }
            // lava...
        }
    }

    private synchronized void restaurarVariables(LavadoraID lavadoraID) {
        if (lavadoraID == LavadoraID.LavadoraA) {
            cargaSimultanea = 0;
            llegadaB = false;
            cedeB = false;
        }
        else {
            cargaSimultanea = 0;
            llegadaA = false;
            cedeA = false;
        }
    }
}
