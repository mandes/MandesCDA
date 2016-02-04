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
package abm.CuiBrabazon;

import java.io.Serializable;
import java.util.Comparator;

public class MarketImpactCompBySize implements Comparator<MarketImpact>, Serializable {
    
    @Override
    public int compare( MarketImpact o1, MarketImpact o2 ) {
        
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if ( o1.id == o2.id ) {
            
            return EQUAL;
        }
        
        if ( o1.orderSize < o2.orderSize) {
            
            return BEFORE; // o1 first
        }
        else {
            
            if ( o1.orderSize < o2.orderSize) { 
                
                return AFTER; // o2 first
            }
            else { // EQUAL
                
                return o1.id < o2.id ? BEFORE : AFTER;  // convention for uniqueness purpose
            }
        }
    }
}

