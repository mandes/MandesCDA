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

import abmlob.agents.Agent;
import abmlob.events.*;
import abmlob.orderbook.Order;
import abmlob.orderbook.OrderCompByOrderTime;
import ccloop.CSVWriter;
import ccloop.MarketState;
import ccloop.TimeStamp;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.TreeSet;

public class CuiMarketState extends MarketState {

    public TimeStamp burnInPeriod;

    //--- specific data
    
    public long noOfMkImpObs;
    public TreeSet<MarketImpact> marketImpactSeries;
    
    public LinkedList<OffSpreadRelLimDist> relLimDistHistory;
    
    public int mmOrdCnt;    // Market Maker orders    
    public int mkOrdCnt;
    public int iocOrders;
        
    public int agCrossLimOrdCnt;    // agent decision
    public int agInSprLimOrdCnt;
    public int agSprLimOrdCnt;
    public int agOffSprLimOrdCnt;
    
    public int effCrossLimOrdCnt;   // effective -- after roundings and classification based on actual book state
    public int effInSprLimOrdCnt;
    public int effSprLimOrdCnt;
    public int effOffSprLimOrdCnt;
    
    public int tradeCnt;
    public int tradeVol;

    public int quoteCount;
    public double avSpread;
    public double avPercSpread;

    public double sumRet;   // trades are not aggregated
    public double sqSumRet;

    public CuiMarketState( long randSeed ) {
        
        super( randSeed );
        
        // override default MatchingEngine
        this.matchingEngine = new MarketImpactMatchingEngine(this, this.orderBook);

        this.burnInPeriod = clock.getCurTime();
        
        this.noOfMkImpObs = 0;
        this.marketImpactSeries = new TreeSet<>( new MarketImpactCompBySize() );    // ordered by size
        
        this.relLimDistHistory = new LinkedList();
    }

    public Order oldestOfTwoOrders( Order o1, Order o2 ) {
        
        // wraps OrderCompByOrderTime, treats also null cases

        if ( o1 == null ) {
            
            if ( o2 == null ) {
                
                return null;
            }
            else {
                
                return o2;
            }
        }
        else {

            if ( o2 == null ) {
                
                return o1;
            }
            else {  // o1, o2 != null
                
                int comp = new OrderCompByOrderTime().compare(o1, o2);
                
                if ( comp == 1 ) {
                    
                    return o1;
                }
                else {
                    
                    return o2;
                }
                
                // comp == 0 means o1 is the same with o2 (same id)
            }
        }
    }
    
    public Order getOldestPopulationOrder( ) {
        
        Order oldestOrder = null;
        
        for (Agent a : agentPop) {
            
            if ( !a.portfolio.buyOrders.isEmpty() ) {

                oldestOrder = oldestOfTwoOrders( oldestOrder, a.portfolio.buyOrders.last() );
            }
            
            if ( !a.portfolio.sellOrders.isEmpty() ) {

                oldestOrder = oldestOfTwoOrders( oldestOrder, a.portfolio.sellOrders.last() );
            }
        }

        return oldestOrder;
    }
    
    @Override
    public void processNextEvent ( EventDispatcher dispatcher ) {
        
        Event nextEvt = eventQueue.queue.first();

        boolean skipFlag = false;
                
        //---- only necessary for the CUI original model

        if ( nextEvt.getClass() == EvtSendNewOrder.class ) {

            Order o = ((EvtSendNewOrder)nextEvt).order;
            
            // if the market order is bigger then book depth, the market maker is called to fill the order book
            
            if ( !o.isLimit ) { // market order
                
                if ( o.isBuy ) { // buy order

                     if ( o.outstanding >= matchingEngine.orderBook.getBookDepth( false ) ) {   // check ask

                         eventQueue.queue.add( new EvtEmptyBook( eventQueue, clock.getCurTime(), 6, false ) );  // higher priority
                         skipFlag = true;
                     }
                }
                else {  // sell order

                     if ( o.outstanding >= matchingEngine.orderBook.getBookDepth( true ) ) {   // check bid
                         
                         eventQueue.queue.add( new EvtEmptyBook( eventQueue, clock.getCurTime(), 6, true ) );
                         skipFlag = true;
                     }
                }
            }
        }

        if ( !skipFlag ) {
            
            eventQueue.queue.pollFirst();

            dispatcher.dispatch( nextEvt, this );            
        
            //---- check order book state after EvtNotifyTrade, EvtRemoveOrder, EvtOrderExpiration

            if ( Arrays.asList( EvtNotifyTrade.class, EvtRemoveOrder.class, EvtOrderExpiration.class ).contains( nextEvt.getClass() ) ) {

                if ( orderBook.bid.isEmpty() ) {

                    eventQueue.queue.add( new EvtEmptyBook( eventQueue, clock.getCurTime(), 6, true ) ); // higher priority
                }
                    
                if ( orderBook.ask.isEmpty() ) {
                    
                    eventQueue.queue.add( new EvtEmptyBook( eventQueue, clock.getCurTime(), 6, false ) );
                }
            }
        }
    }

    public void saveMarketImpactToFile( String fileName, int config, int run, boolean Microsoft ) {
        
        PrintWriter pw = null;
        CSVWriter csv = null;
                
        char delimiter = Microsoft ? ';' : ',';

        try {

            pw = new PrintWriter( fileName );
            csv = new CSVWriter(pw, false, delimiter, System.getProperty("line.separator") );

            Iterator<MarketImpact> mkImpItr = marketImpactSeries.iterator();
            
            while ( mkImpItr.hasNext() ) {
                
                MarketImpact t = mkImpItr.next();
                
                csv.write( String.format( "%d", config ) );
                csv.write( String.format( "%d", run ) );
                //csv.write( String.format( "%d", t.id ) );

                if ( Microsoft ) {

                    csv.write( String.format( "%d", t.orderSize ) );
                    csv.write( String.format( "%f", t.logMidQuoteChange ) );
                }
                else {

                    csv.write( String.format( Locale.US,"%d", t.orderSize ) );
                    csv.write( String.format( Locale.US,"%f", t.logMidQuoteChange ) );
                }

                csv.writeln();
            }

        }
        catch ( Exception ex ) {
            
            ex.printStackTrace();
        }
	finally {

            if ( pw != null ) { pw.close(); }
            if ( csv != null ) { csv.close(); }
        }
    }

    public void saveOffSpreadRelLimDistToFile( String fileName, int config, int run, boolean Microsoft ) {
        
        PrintWriter pw = null;
        CSVWriter csv = null;
                
        char delimiter = Microsoft ? ';' : ',';

        try {

            pw = new PrintWriter( fileName );
            csv = new CSVWriter(pw, false, delimiter, System.getProperty("line.separator") );

            Iterator<OffSpreadRelLimDist> relLimDistItr = relLimDistHistory.iterator();
            
            while ( relLimDistItr.hasNext() ) {
                
                OffSpreadRelLimDist t = relLimDistItr.next();
                
                csv.write( String.format( "%d", config ) );
                //csv.write( String.format( "%d", run ) );

                if ( Microsoft ) {

                    csv.write( String.format( "%d", t.dist ) );
                }
                else {

                    csv.write( String.format( Locale.US,"%d", t.dist ) );
                }

                csv.writeln();
            }

        }
        catch ( Exception ex ) {
            
            ex.printStackTrace();
        }
	finally {

            if ( pw != null ) { pw.close(); }
            if ( csv != null ) { csv.close(); }
        }
    }

}
