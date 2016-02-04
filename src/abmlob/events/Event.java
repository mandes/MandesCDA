/* 
 * Copyright 2015 Alexandru Mandes.
 *
 * The code is available under a MIT License.
 *
 * Please cite: Mandes, A. (2015). Microstructure-based order placement in a 
 * continuous double auction agent based model, Algorithmic Finance 4:3-4, 
 * pp. 105-125. DOI: 10.3233/AF-150049. 
 *
 * Further reference: Cui, W. and Brabazon, A. (2012). An agent-based modeling 
 * approach to study price impact, Computational Intelligence for Financial 
 * Engineering & Economics (CIFEr), 2012 IEEE Conference on [proceedings], IEEE Press.
 */
package abmlob.events;

import ccloop.MyException;
import ccloop.TimeStamp;

public abstract class Event {

    public long id;
    
    public TimeStamp eventTime;
    public int priority = 5;    // default
    
    public Event( EventQueue queue ) {

        if ( queue.noOfEvents == Long.MAX_VALUE ) {
            
            throw new MyException("Event: id numeric overflow");
        }
        
        queue.noOfEvents++;
        id = queue.noOfEvents;
    }
    
    public Class<? extends Event> getType() {

        return getClass();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
    
    // not necessary for TreeSet
    @Override 
    public boolean equals(final Object obj) {
        
        if (obj == null || this.getClass() != obj.getClass()) {
            
           return false;
        }
        
        return ( this.id == ((Event)obj).id );
    }

    @Override
    public String toString() { 

        StringBuffer buf = new StringBuffer();
        
        buf.append(id).append("-").append(eventTime).append("-").append(getType());
        
        return(buf.toString());
    }

}
