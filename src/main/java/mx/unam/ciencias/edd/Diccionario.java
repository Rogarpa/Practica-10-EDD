package mx.unam.ciencias.edd;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
* Clase para diccionarios (<em>hash tables</em>). Un diccionario generaliza el
* concepto de arreglo, mapeando un conjunto de <em>llaves</em> a una colección
* de <em>valores</em>.
*/
public class Diccionario<K, V> implements Iterable<V> {
    
    /* Clase interna privada para entradas. */
    private class Entrada {
        
        /* La llave. */
        public K llave;
        /* El valor. */
        public V valor;
        
        /* Construye una nueva entrada. */
        public Entrada(K llave, V valor) {
            this.llave = llave;
            this.valor = valor;
        }
    }
    
    /* Clase interna privada para iteradores. */
    private class Iterador {
        
        /* En qué lista estamos. */
        private int indice;
        /* Iterador auxiliar. */
        private Iterator<Entrada> iterador;
        
        /* Construye un nuevo iterador, auxiliándose de las listas del
        * diccionario. */
        public Iterador() {
            mueveIterador();
        }
        
        /* Nos dice si hay una siguiente entrada. */
        public boolean hasNext() {
            return iterador != null;
        }
        
        /* Regresa la siguiente entrada. */
        public Entrada siguiente() {
            if(iterador == null)
            throw new NoSuchElementException();
            
            Entrada next = iterador.next();
            
            if(!(iterador.hasNext())){ 
                iterador = null;
                mueveIterador();
            }
            
            return next;
            
        }
    
        /* Mueve el iterador a la siguiente entrada válida. */
        private void mueveIterador() {
            int longitudEntradas = entradas.length;
            indice++;
            while(indice < longitudEntradas){
                if(entradas[indice] != null){
                    iterador = entradas[indice].iterator();
                    return;
                }
                indice++;
            }
            
        }
    }
    
    
        
        /* Clase interna privada para iteradores de llaves. */
        private class IteradorLlaves extends Iterador
        implements Iterator<K> {
            
            /* Regresa el siguiente elemento. */
            @Override public K next() {
                super.mueveIterador();
                return siguiente().llave;
            }
        }
        
        /* Clase interna privada para iteradores de valores. */
        private class IteradorValores extends Iterador
        implements Iterator<V> {
            
            /* Regresa el siguiente elemento. */
            @Override public V next() {
                super.mueveIterador();
                return siguiente().valor;
            }
        }
        
        /** Máxima carga permitida por el diccionario. */
        public static final double MAXIMA_CARGA = 0.72;
        
        /* Capacidad mínima; decidida arbitrariamente a 2^6. */
        private static final int MINIMA_CAPACIDAD = 64;
        
        /* Dispersor. */
        private Dispersor<K> dispersor;
        /* Nuestro diccionario. */
        private Lista<Entrada>[] entradas;
        /* Número de valores. */
        private int elementos;
        
        /* Truco para crear un arreglo genérico. Es necesario hacerlo así por cómo
        Java implementa sus genéricos; de otra forma obtenemos advertencias del
        compilador. */
        @SuppressWarnings("unchecked")
        private Lista<Entrada>[] nuevoArreglo(int n) {
            return (Lista<Entrada>[])Array.newInstance(Lista.class, n);
        }
        
        /**
        * Construye un diccionario con una capacidad inicial y dispersor
        * predeterminados.
        */
        public Diccionario() {
            dispersor = (K llave) -> llave.hashCode();
            entradas = nuevoArreglo(MINIMA_CAPACIDAD);
        }
        
        /**
        * Construye un diccionario con una capacidad inicial definida por el
        * usuario, y un dispersor predeterminado.
        * @param capacidad la capacidad a utilizar.
        */
        public Diccionario(int capacidad) {
            dispersor = (K llave) -> llave.hashCode();
            if(capacidad < MINIMA_CAPACIDAD){
                entradas = nuevoArreglo(MINIMA_CAPACIDAD);
                
            }else{
                int i = 1;
                int nuevaCapacidad = 2*capacidad;
                while(i < nuevaCapacidad)
                    i = i<<1;
                
                entradas = nuevoArreglo(i);}
                
            }

        
    
            
            /**
            * Construye un diccionario con una capacidad inicial predeterminada, y un
            * dispersor definido por el usuario.
            * @param dispersor el dispersor a utilizar.
            */
            public Diccionario(Dispersor<K> dispersor) {
                this.dispersor = dispersor;
            }
            
            /**
            * Construye un diccionario con una capacidad inicial y un método de
            * dispersor definidos por el usuario.
            * @param capacidad la capacidad inicial del diccionario.
            * @param dispersor el dispersor a utilizar.
            */
            public Diccionario(int capacidad, Dispersor<K> dispersor) {
                this.dispersor = dispersor;
                if(capacidad < MINIMA_CAPACIDAD){
                    entradas = nuevoArreglo(MINIMA_CAPACIDAD);
                    return;
                }
                
                int i = 1;
                int nuevaCapacidad = 2*capacidad;
                while(i < nuevaCapacidad)
                i = i<<1;
                
                entradas = nuevoArreglo(i);
                
                
            }
            
            /**
            * Agrega un nuevo valor al diccionario, usando la llave proporcionada. Si
            * la llave ya había sido utilizada antes para agregar un valor, el
            * diccionario reemplaza ese valor con el recibido aquí.
            * @param llave la llave para agregar el valor.
            * @param valor el valor a agregar.
            * @throws IllegalArgumentException si la llave o el valor son nulos.
            */
            public void agrega(K llave, V valor) {
                if(llave == null | valor == null) throw new IllegalArgumentException();
                
                int i = (dispersor.dispersa(llave) & (entradas.length - 1));
                
                if(entradas[i] == null){
                    Lista<Entrada> nuevaLista = new Lista<Entrada>();
                    entradas[i] = nuevaLista;
                    nuevaLista.agrega(new Entrada(llave, valor));
                    elementos++;
                }
                else{
                    for(Entrada e: entradas[i])
                    if(e.llave.equals(llave)){
                        e.valor = valor;
                        verificarCarga();
                        return;
                    }
                    entradas[i].agrega(new Entrada(llave, valor));
                    elementos++;
                    verificarCarga();
                    
                }
            }
            
            private void verificarCarga(){
                Lista<Entrada>[] viejasEntradas = entradas;
                entradas = nuevoArreglo(2*viejasEntradas.length);
                
                int i = 0;
                for(Lista<Entrada> l: viejasEntradas)
                    entradas[i++] = l;
                
            }
            
            /**
            * Regresa el valor del diccionario asociado a la llave proporcionada.
            * @param llave la llave para buscar el valor.
            * @return el valor correspondiente a la llave.
            * @throws IllegalArgumentException si la llave es nula.
            * @throws NoSuchElementException si la llave no está en el diccionario.
            */
            public V get(K llave) {
                if(llave == null)
                throw new IllegalArgumentException();
                int i = dispersor.dispersa(llave)&(entradas.length-1);
                if(entradas[i] == null)
                    throw new NoSuchElementException();
                else{
                    for(Entrada e: entradas[i])
                    if(e.llave.equals(llave))
                    return e.valor;
                    
                    throw new NoSuchElementException();
                }
                
            }
            
            /**
            * Nos dice si una llave se encuentra en el diccionario.
            * @param llave la llave que queremos ver si está en el diccionario.
            * @return <code>true</code> si la llave está en el diccionario,
            *         <code>false</code> en otro caso.
            */
            public boolean contiene(K llave) {
                try {
                    get(llave);
                    return true;
                } catch (Exception e) {
                    return false;
                }
                
            }
            
            /**
            * Elimina el valor del diccionario asociado a la llave proporcionada.
            * @param llave la llave para buscar el valor a eliminar.
            * @throws IllegalArgumentException si la llave es nula.
            * @throws NoSuchElementException si la llave no se encuentra en
            *         el diccionario.
            */
            public void elimina(K llave) {
                if(llave == null)
                throw new IllegalArgumentException();
                int i = dispersor.dispersa(llave)&(entradas.length-1);
                
                if(entradas[i] == null) 
                throw new NoSuchElementException();

                for(Entrada e: entradas[i])
                    if(e.llave.equals(llave)){
                        entradas[i].elimina(e);
                        elementos--;
                        return;
                    }
                
                throw new NoSuchElementException();
                
            }
            
            /**
            * Nos dice cuántas colisiones hay en el diccionario.
            * @return cuántas colisiones hay en el diccionario.
            */
            public int colisiones() {
                int colisiones = 0;
                int i = -1;
                while(i < entradas.length)
                colisiones += entradas[i++].getElementos();
                return colisiones;
            }
            
            /**
            * Nos dice el máximo número de colisiones para una misma llave que tenemos
            * en el diccionario.
            * @return el máximo número de colisiones para una misma llave.
            */
            public int colisionMaxima() {
                int max = 0;
                int i = 0;
                while(i < entradas.length)
                    if(max < entradas[i].getElementos()) 
                        max = entradas[i++].getElementos();
                return max;
            }
            
            /**
            * Nos dice la carga del diccionario.
            * @return la carga del diccionario.
            */
            public double carga() {
                return elementos/entradas.length;
            }
            
            /**
            * Regresa el número de entradas en el diccionario.
            * @return el número de entradas en el diccionario.
            */
            public int getElementos() {
                return elementos;
            }
            
            /**
            * Nos dice si el diccionario es vacío.
            * @return <code>true</code> si el diccionario es vacío, <code>false</code>
            *         en otro caso.
            */
            public boolean esVacia() {
                return elementos == 0;
            }
            
            /**
            * Limpia el diccionario de elementos, dejándolo vacío.
            */
            public void limpia() {
                entradas = nuevoArreglo(entradas.length);
                elementos = 0;
            }
            
            /**
            * Regresa una representación en cadena del diccionario.
            * @return una representación en cadena del diccionario.
            */
            @Override public String toString() {
                return "";
            }
            
            
            /**
            * Nos dice si el diccionario es igual al objeto recibido.
            * @param o el objeto que queremos saber si es igual al diccionario.
            * @return <code>true</code> si el objeto recibido es instancia de
            *         Diccionario, y tiene las mismas llaves asociadas a los mismos
            *         valores.
            */
            @Override public boolean equals(Object o) {
                if (o == null || getClass() != o.getClass())
                return false;
                @SuppressWarnings("unchecked") Diccionario<K, V> d =
                (Diccionario<K, V>)o;
                
                return true;
            }
            
            /**
            * Regresa un iterador para iterar las llaves del diccionario. El
            * diccionario se itera sin ningún orden específico.
            * @return un iterador para iterar las llaves del diccionario.
            */
            public Iterator<K> iteradorLlaves() {
                return new IteradorLlaves();
            }
            
            /**
            * Regresa un iterador para iterar los valores del diccionario. El
            * diccionario se itera sin ningún orden específico.
            * @return un iterador para iterar los valores del diccionario.
            */
            @Override public Iterator<V> iterator() {
                return new IteradorValores();
            }
        }
    
    