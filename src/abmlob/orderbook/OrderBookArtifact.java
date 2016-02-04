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

import ccloop.MyException;

public abstract class OrderBookArtifact {

    protected long id;

    public OrderBookArtifact(){}
    
    public OrderBookArtifact( OrderBook ob ){

        if ( ob.noOfArtifacts == Long.MAX_VALUE ) {
            
            throw new MyException("OrderBookArtifact: id numeric overflow");
        }
        
        ob.noOfArtifacts++;
        id = ob.noOfArtifacts;
    }
    
    public long getId() {
        
        return id;
    }
}
