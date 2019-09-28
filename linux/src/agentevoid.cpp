#include "agentevoid.h"
#include "gencarga.h"
#include <semaphore.h>

sem_t scLavado, mutex, syncA, syncB;
int cargaSimultanea;

AgenteVoid::AgenteVoid(GenCarga& genCarga) : genCarga(genCarga) { 
    cargaSimultanea = this->genCarga.obtenerCargaMax();
    sem_init(&syncA, 0, 0); 
    sem_init(&syncB, 0, 0); 
    sem_init(&scLavado, 0, 1); // Semaforo de seccion critica de lavado
    sem_init(&mutex, 0, 1); // Semaforo de seccion critica de cargaSimultanea
}

AgenteVoid::~AgenteVoid() { }

void AgenteVoid::arrancar(LavadoraID lavadoraID, int carga) {
    sem_wait(&mutex); // Accede a seccion critica
    cargaSimultanea = cargaSimultanea - carga;
    sem_post(&mutex); // Sale de seccion critica
    if (lavadoraID == LavadoraA) {
        sem_post(&syncA); // A avisa que ya llego
        sem_wait(&syncB); // A espera por B para seguir
    }
    else {
        sem_post(&syncB); // B avisa que ya llego
        sem_wait(&syncA); // B espera por A para seguir
    }
    if (cargaSimultanea < 0) { // Si la carga es mayor a la maxima
        sem_wait(&scLavado); // Entra a seccion critica de lavado (una a la vez)
        // lava...
    }
    // si no cumple al cond, pueden lavar al mismo tiempo
}

void AgenteVoid::parar(LavadoraID lavadoraID) {
    if (cargaSimultanea < 0) { // Si la carga es mayor a la maxima...
        sem_post(&scLavado); // ... libera la seccion critica para que la otra lave
    }
    sem_wait(&mutex); // Seccion critica para reiniciar la carga simultanea
    cargaSimultanea = this->genCarga.obtenerCargaMax(); // Reinicia la carga simultanea en la carga maxima
    sem_post(&mutex); // Sale de la seccion critica de reinicio
    if (lavadoraID == LavadoraA) {
        sem_post(&syncA); // A avisa que ya llego
        sem_wait(&syncB); // A espera por B
    }
    else {
        sem_post(&syncB); // B avisa que ya llego
        sem_wait(&syncA); // B espera por A
    }
}
