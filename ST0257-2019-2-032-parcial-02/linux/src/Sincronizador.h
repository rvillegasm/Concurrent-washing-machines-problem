#pragma once
#include <LavadoraID>

class Sincronizador {
 public:
  Sinc();
  virtual ~Sinc();
  virtual void arrancar(LavadoraID lavadoraID, int carga) = 0;
  virtual void parar(LavadoraID lavadoraID) = 0;
};
