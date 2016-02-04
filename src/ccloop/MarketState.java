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
package ccloop;

import abmlob.agents.*;
import abmlob.events.*;
import abmlob.orderbook.*;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

public class MarketState {
    
    //---- functionals
    
    public RandNumGen rng;
    
    //---- model related
    
    public CentralClock clock;
    public EventQueue eventQueue;

    public int instancedAgents;
    public ArrayList<Agent> agentPop;
    
    public OrderBook orderBook;
    public MatchingEngine matchingEngine;
    
    public Deque<Trade> tradeHistory;   // double ended queue
    public LinkedList<Quote> quoteHistory;
    public LinkedList<FundValue> fundValueHistory;
    
    public MarketState( long randSeed ) {

        this.rng = new RandNumGen( randSeed );
        
        this.eventQueue = new EventQueue();
        this.clock = new CentralClock();

        this.instancedAgents = 0;
        this.agentPop = new ArrayList<>();
                
        this.orderBook = new OrderBook();
        this.matchingEngine = new MatchingEngine(this, this.orderBook);
        
        this.tradeHistory = new ArrayDeque();
        this.quoteHistory = new LinkedList();
        this.fundValueHistory = new LinkedList();
    }

    public void processNextEvent ( EventDispatcher dispatcher ) {
        
        Event nextEvt = eventQueue.queue.first();
        
        eventQueue.queue.pollFirst();

        dispatcher.dispatch( nextEvt, this );
    }
    
    public void addAgent(Agent agent) {

        agentPop.add(agent);
        
        // persist agent to database
    }

    public void addTrade(Trade trade) {

        tradeHistory.addLast(trade);
    }
    
    public void addQuote(Quote quote) {

        quoteHistory.add(quote);
    }

    public void addFundValue(FundValue fv) {

        fundValueHistory.addLast(fv);
    }
    
    public void resetCount() {
        
        instancedAgents = 0;
    }

    public void saveTradesToFile( String fileName, TimeStamp leftCut, boolean Microsoft ) {
        
        PrintWriter pw = null;
        CSVWriter csv = null;
                
        char delimiter = Microsoft ? ';' : ',';

        try {

            pw = new PrintWriter( fileName );
            csv = new CSVWriter(pw, false, delimiter, System.getProperty("line.separator") );

            Iterator<Trade> tradesItr = tradeHistory.iterator();
            
            while ( tradesItr.hasNext() ) {
                
                Trade t = tradesItr.next();
                
                if ( t.time.compareTo(leftCut) == 1 ) {

                    csv.write( String.format( "%d", t.getId() ) );
                    
                    if ( Microsoft ) {

                        //pw.write( t.size + ";" + t.price + "\n");
                        csv.write( String.format( "%d", t.size ) );
                        csv.write( String.format( "%d", t.price ) );
                    }
                    else {

                        csv.write( String.format( Locale.US,"%d", t.size ) );
                        csv.write( String.format( Locale.US,"%d", t.price ) );
                    }

                    csv.write( String.format( "%b", t.buyerInit ) );
                    csv.writeln();
                }
            }
        }
        catch (Exception ex) {
            
            ex.printStackTrace();
        }
	finally {

            if ( pw != null ) { pw.close(); }
            if ( csv != null ) { csv.close(); }
        }
    }

    public void saveQuotesToFile( String fileName, TimeStamp leftCut ) {
        
        PrintWriter pw = null;

        try {

            pw = new PrintWriter( fileName );

            Iterator<Quote> quotesItr = quoteHistory.listIterator();
            
            pw.write("BestBid; BestAsk; Spread; MidPoint\n");
            
            while ( quotesItr.hasNext() ) {
                
                Quote q = quotesItr.next();
                
                if ( q.time.compareTo(leftCut) == 1 && q.bestBid > 0 && q.bestAsk > 0 ) {
                    
                    pw.write( q.bestBid + ";" + q.bestAsk + ";" + (q.bestAsk - q.bestBid) + ";" + ((q.bestAsk + q.bestBid) / 2) + "\n");
                }
            }
        }
        catch (Exception ex) {
            
            ex.printStackTrace();
        }
	finally {

            if ( pw != null ) { pw.close(); }
        }
    }

    public void saveFundValToFile( String fileName, TimeStamp leftCut, boolean Microsoft ) {
        
        PrintWriter pw = null;
        CSVWriter csv = null;

        char delimiter = Microsoft ? ';' : ',';
        
        try {

            pw = new PrintWriter( fileName );
            csv = new CSVWriter(pw, false, delimiter, System.getProperty("line.separator") );

            Iterator<FundValue> fundamItr = fundValueHistory.listIterator();
            
            while ( fundamItr.hasNext() ) {
                
                FundValue f = fundamItr.next();
                        
                if ( f.time.compareTo(leftCut) == 1 ) {

                    if ( Microsoft ) {

                        //pw.write( fundamItr.next().value + "\n");
                        csv.writeln( String.format( "%10.4f", f.value ) );
                    }
                    else {

                        csv.writeln( String.format( Locale.US,"%10.4f", f.value ) );
                    }
                }
            }
        }
        catch (Exception ex) {
            
            ex.printStackTrace();
        }
	finally {

            if ( pw != null ) { pw.close(); }
            
            if (csv != null) { csv.close(); }
        }
    }

}
