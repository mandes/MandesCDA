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

import ccloop.MyException;

public class MarketImpact {

    public long id;

    public int orderSize;
    public double logMidQuoteChange;
    
    public MarketImpact( CuiMarketState mState, int orderSize, double logMidQuoteChange ) { 

        if ( mState.noOfMkImpObs == Long.MAX_VALUE ) {
            
            throw new MyException("MarketImpact: id numeric overflow");
        }
        
        mState.noOfMkImpObs++;
        id = mState.noOfMkImpObs;

        this.orderSize = orderSize;
        this.logMidQuoteChange = logMidQuoteChange;
    }

    @Override
    public int hashCode() {
        
        int hash = 7;
        hash = 29 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        
        if ( obj == null || getClass() != obj.getClass() ) {
            
            return false;
        }
        
        final MarketImpact other = (MarketImpact) obj;
        
        if ( this.id != other.id ) {
            
            return false;
        }
        
        return true;
    }
    
}
