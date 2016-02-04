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
package abmlob.orderbook;

import java.io.Serializable;
import java.util.Comparator;

public class OrderCompByBuyPriority implements Comparator<Order>, Serializable {
    
    @Override
    public int compare(Order o1, Order o2) {

        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (o1.getId() == o2.getId()) {
            
            return EQUAL;
        }
        
        if ( o1.limitPrice > o2.limitPrice) {
            
            return BEFORE; // o1 first
        }
        else {
            
            if ( o1.limitPrice < o2.limitPrice) { 
                
                return AFTER; // o2 first
            }
            else // ==
            {

                int timeStampComp = o1.priorityTime.compareTo(o2.priorityTime);
                
                if ( timeStampComp == -1) {

                    return BEFORE;
                }
                else {
                    if ( timeStampComp == 1) {

                        return AFTER;
                    }
                    else {  // EQUAL
                        
                        return o1.getId() < o2.getId() ? BEFORE : AFTER;  // convention for uniqueness purpose
                    }
                }
            }
        }
    }
}
