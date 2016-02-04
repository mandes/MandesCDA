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

import java.io.Serializable;
import java.util.Comparator;

public class EventCompByTimePriority implements Comparator<Event>, Serializable {
    
    @Override
    public int compare( Event e1, Event e2 ) {

        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;
        
        if ( e1.id == e2.id ) {
            
            return EQUAL;
        }
        
        int timeStampComp = e1.eventTime.compareTo(e2.eventTime);
        
        if ( timeStampComp == -1) {
            
            return BEFORE;
        }
        else {
            if ( timeStampComp == 1) {
                
                return AFTER;
            }
            else {  // EQUAL
                
                if ( e1.priority > e2.priority ) {
                    
                    return BEFORE;
                }
                else {
                    
                    if ( e1.priority < e2.priority ) {
                        
                        return AFTER;
                    }
                    else { // ==
                        
                        return e1.id < e2.id ? BEFORE : AFTER;  // convention for uniqueness purpose
                    }
                }
            }
        }
    }
}
