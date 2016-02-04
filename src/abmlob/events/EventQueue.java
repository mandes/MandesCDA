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

import abmlob.orderbook.Order;
import ccloop.MyException;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

public class EventQueue {

    public long noOfEvents;
    public TreeSet<Event> queue;   // event multiplexing
    
    public EventQueue () {

        EventCompByTimePriority comp = new EventCompByTimePriority();
        
        this.noOfEvents = 0;
        this.queue = new TreeSet<>(comp);
    }
    
    public void removeEventAssociatedWith ( Order o, Event source ) {
        
        Map<Event, Object> toBeRemoved = new IdentityHashMap<Event, Object>();
        
        Iterator<Event> itr = queue.iterator();
        
        while( itr.hasNext() ){
            
            Event evt = itr.next();
            
            if (! evt.equals(source) ) { // only different (future?) events
            
                if ( evt.getClass() == EvtRemoveOrder.class ) {

                    if ( ((EvtRemoveOrder)evt).order.getId() == o.getId() ) {

                        toBeRemoved.put(evt, null);
                    }
                }

                if ( evt.getClass() == EvtOrderExpiration.class ) {

                    if ( ((EvtOrderExpiration)evt).order.getId() == o.getId() ) {

                        toBeRemoved.put(evt, null);
                    }
                }
                
                // or others
            }
        }
        
        if ( !toBeRemoved.isEmpty() ) {
            
            if ( !queue.removeAll(toBeRemoved.keySet()) ) {

                throw new MyException("EventQueue.removeEventAssociatedWith - could not remove from event queue");
            }

            toBeRemoved.clear();    // ??? precaution needed / local variable
        }
    }
    
    @Override
    public String toString() {

	StringBuilder buf = new StringBuilder();
        int i = 1;

        buf.append("EventQueue (").append(queue.size()).append(" events)\n");
        
        for (Event ev : queue) {
            
            buf.append(i).append(".").append(ev).append("\n");
            i++;
        }

        return(buf.toString());
    }
}
