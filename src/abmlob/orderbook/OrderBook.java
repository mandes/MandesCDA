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

import ccloop.*;
import java.util.Iterator;
import java.util.TreeSet;

public class OrderBook {
    
    public long noOfArtifacts;
    
    public TreeSet<Order> bid;
    public TreeSet<Order> ask;
    
    public int decCorr;    // decimalCorrection
    
    public OrderBook() {

        this.noOfArtifacts = 0;
        
        this.bid = new TreeSet<>(new OrderCompByBuyPriority());
        this.ask = new TreeSet<>(new OrderCompBySellPriority());
        
        decCorr = ( Consts.PRICEDIGITS - Consts.CASHDIGITS ) > 0 ? ( Consts.PRICEDIGITS - Consts.CASHDIGITS ) : 0;
    }

    public OrderBook( OrderBook source ) {

        this.noOfArtifacts = source.noOfArtifacts;
        
        this.bid = new TreeSet<>(new OrderCompByBuyPriority());        
        this.ask = new TreeSet<>(new OrderCompBySellPriority());
        
        this.decCorr = source.decCorr;
    }

    // order book add function, which test that no buy order is send to ask or conversely
    
    public WorkingQuote getWorkingQuote( TimeStamp t ) {
        
        int bestBid = 0, bestBidVol = 0,
            bestAsk = 0, bestAskVol = 0;

        Iterator<Order> bidIterator = this.bid.iterator();

        if ( bidIterator.hasNext() ) {

            Order b = bidIterator.next();
            bestBid = b.limitPrice;
            bestBidVol += b.outstanding;
        }

        while ( bidIterator.hasNext() ) {

            Order o = bidIterator.next();
            
            if ( o.limitPrice == bestBid ) {
                
                bestBidVol += o.outstanding;    
            }
            else {

                break;
            }
        }

        Iterator<Order> askIterator = this.ask.iterator();

        if ( askIterator.hasNext() ) {

            Order a = askIterator.next();
            bestAsk = a.limitPrice;
            bestAskVol += a.outstanding;
        }

        while ( askIterator.hasNext() ) {

            Order o = askIterator.next();
            
            if ( o.limitPrice == bestAsk ) {
                
                bestAskVol += o.outstanding;    
            }
            else {

                break;
            }
        }

        return new WorkingQuote(t, bestBid, bestBidVol, bestAsk, bestAskVol);
    }

    public WorkingQuote getBidAskSpread( TimeStamp t ) {
        
        int bestBid = 0, bestAsk = 0;

        if ( !bid.isEmpty() ) {

            bestBid = bid.first().limitPrice;
        }

        if ( !ask.isEmpty() ) {

            bestAsk = ask.first().limitPrice;
        }

        return new WorkingQuote(t, bestBid, 0, bestAsk, 0);
    }

    public int getBookDepth( boolean bid ) {
        
        int depth = 0;
        
        Iterator<Order> bookIterator;
        
        if ( bid ) {
            
            bookIterator = this.bid.iterator();
        }
        else {
            
            bookIterator = this.ask.iterator();
        }
        
        while ( bookIterator.hasNext() ) {

            depth += bookIterator.next().outstanding;
        }
        
        return depth;
    }
    
    public String printDetailed() { 

        StringBuffer buf = new StringBuffer();
        int i = 1, j = 1;

        buf.append("OrderBook Bid (").append(bid.size()).append(" orders)\n");
        for (Order o : bid) {

            buf.append(i).append(".").append(o).append("\n");
            i++;
        }

        buf.append("OrderBook Ask (").append(ask.size()).append(" orders)\n");
        for (Order o : ask) {

            buf.append(j).append(".").append(o).append("\n");
            j++;
        }
        
        return(buf.toString());
    }

    @Override
    public String toString() { 

        StringBuffer buf = new StringBuffer();

        //---- Bid side
        
        buf.append("OrderBook Bid (").append(bid.size()).append(" orders)\n");

        Iterator<Order> bidIterator = bid.iterator();
        int levelCount = 0, levelPrice = 0, aggSize = 0, aggCount = 0;

        if ( bidIterator.hasNext() ) {
        
            Order b = bidIterator.next();
            
            levelCount = 1;
            levelPrice = b.limitPrice;
            aggCount = 1;
            aggSize = b.outstanding;
        }

        while ( bidIterator.hasNext() ) {

            Order b = bidIterator.next();

            if ( b.limitPrice != levelPrice ) {
                
                buf.append(levelCount).append(". ").append(aggSize).append("(").append(aggCount).append(" ord.) @").
                        append( (double)levelPrice / Math.pow(10,Consts.PRICEDIGITS) ).append("\n");

                levelCount++;
                levelPrice = b.limitPrice;
                aggCount = 1;
                aggSize = b.outstanding;
            }
            else {
                
                aggCount++;
                aggSize += b.outstanding;
            }
        }

        if ( levelCount > 0 ) {
            
            buf.append(levelCount).append(". ").append(aggSize).append("(").append(aggCount).append(" ord.) @").
                    append( (double)levelPrice / Math.pow(10,Consts.PRICEDIGITS) ).append("\n");
        }
        
        //---- Ask side
        
        buf.append("OrderBook Ask (").append(ask.size()).append(" orders)\n");

        Iterator<Order> askIterator = ask.iterator();
        levelCount = levelPrice = aggSize = aggCount = 0;
                
        if ( askIterator.hasNext() ) {
        
            Order a = askIterator.next();
            
            levelCount = 1;
            levelPrice = a.limitPrice;
            aggCount = 1;
            aggSize = a.outstanding;
        }

        while ( askIterator.hasNext() ) {

            Order a = askIterator.next();

            if ( a.limitPrice != levelPrice ) {
                
                buf.append(levelCount).append(". ").append(aggSize).append("(").append(aggCount).append(" ord.) @").
                        append( (double)levelPrice / Math.pow(10,Consts.PRICEDIGITS) ).append("\n");
                
                levelCount++;
                levelPrice = a.limitPrice;
                aggCount = 1;
                aggSize = a.outstanding;
            }
            else {
                
                aggCount++;
                aggSize += a.outstanding;
            }
        }

        if ( levelCount > 0 ) {
            
            buf.append(levelCount).append(". ").append(aggSize).append("(").append(aggCount).append(" ord.) @").
                    append( (double)levelPrice / Math.pow(10,Consts.PRICEDIGITS) ).append("\n");
        }

        return( buf.toString() );
    }

}
