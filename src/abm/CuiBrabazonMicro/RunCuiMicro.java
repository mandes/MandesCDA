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
package abm.CuiBrabazonMicro;

import ccloop.Consts;
import ccloop.MyException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class RunCuiMicro {
    
    public static void main(String[] args) {
       
        // During the continuous trading session, there are 30,600,000 milliseconds in each trading day.
        // One day + 1 Hour (burn in) = 34,200,000
        
        Consts.TIMETICKSPERDAY = 34200000; // 7200000

        Consts.DEBUGMODE = false;    // fast debugging
        Consts.DEBUGSUMMARY = false;
        Consts.DEBUGDISK = true;
        
        long randSeeds[] = {6548412, 44, 993, 2, 213215, 1165465, 31358468, 684633, 11111, 1232, 586, 6466, 
                            734735, 4148468, 321654, 131553, 3265, 4654, 645, 9863, 384633, 439879,
                            9336543, 68746, 35422, 5843, 796663, 43433, 64786, 9433};
        int totalRuns = 1;  // 30

        if ( totalRuns > randSeeds.length ) {
            
            throw new MyException("Not enough random seed values.");
        }
        
        try {

            //----- init DB connection

            //----- run simulation
            
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String path = "C:/Data/Micro 5/";
/*
            for i in *.csv; do
                sed -i "s/;[0-9]\+;/;/g" $i
            done
*/
            ParamSet params = new ParamSet();   // default params

        int totalConfigs = 1;   // 100
        Random r = new Random( 100 );

        for ( int k = 1; k <= totalConfigs; k++ ) {

            System.out.println("Config #" + k + " started.");
            
            ParamSet randParams = new ParamSet();

            // randomize params: uniformly within a +/- 30% range around the default values
            if ( k > 1 ) {
            
                randParams.obiBase = params.obiBase * ( 0.7 + r.nextDouble() * 0.6 );
                randParams.obiLevels = 1 + r.nextInt(3);    // int: 1,2,3
                randParams.sizePenaltyExp = params.sizePenaltyExp * ( 0.7 + r.nextDouble() * 0.6 );
                randParams.isSigmaMult = params.isSigmaMult * ( 0.7 + r.nextDouble() * 0.6 );
                randParams.dynSigmaMult = params.dynSigmaMult * ( 0.7 + r.nextDouble() * 0.6 );
                randParams.alpha0 = params.alpha0 * ( 0.7 + r.nextDouble() * 0.6 );
                randParams.alpha1 = params.alpha1 * ( 0.7 + r.nextDouble() * 0.6 );
                randParams.alpha2 = params.alpha2 * ( 0.7 + r.nextDouble() * 0.6 );
                randParams.beta = params.beta * ( 0.7 + r.nextDouble() * 0.6 );
                
                randParams.mu1 = params.mu1 * ( 0.7 + r.nextDouble() * 0.6 );
                randParams.sigma1 = params.sigma1 * ( 0.7 + r.nextDouble() * 0.6 );
                randParams.mu2 = params.mu2 * ( 0.7 + r.nextDouble() * 0.6 );
                randParams.sigma2 = params.sigma2 * ( 0.7 + r.nextDouble() * 0.6 );
                randParams.p1 = params.p1 * ( 0.7 + r.nextDouble() * 0.6 );
            }
            
            System.out.println(randParams);
            
            for ( int i = 1; i <= totalRuns; i++ ) {
                
                System.out.println("Run #" + i + " started.");
                
                CuiMicroABModel abm = new CuiMicroABModel( randParams, randSeeds[i-1] );
                
                abm.run();
                
                //--- results per run
                
                if ( Consts.DEBUGSUMMARY ) {

                    int totalLim = abm.state.effCrossLimOrdCnt + abm.state.effInSprLimOrdCnt + abm.state.effSprLimOrdCnt + abm.state.effOffSprLimOrdCnt;
                    int totalOrd = abm.state.mkOrdCnt + totalLim;

                    // Cui Benchmarks: mk ord (3.75%), lim ord (96.25%) = in (10.10%) + off (89.90%)

                    System.out.println("# Market Maker orders: " + abm.state.mmOrdCnt + "(" + ( (double)abm.state.mmOrdCnt * 100 / ( abm.state.mmOrdCnt + totalOrd ) ) + "%)");

                    System.out.println("# Agent market orders: " + abm.state.mkOrdCnt + "(" + ( (double)abm.state.mkOrdCnt * 100 / totalOrd ) + "%)");
                    System.out.println("# Agent IOC market orders: " + abm.state.iocOrders + "(" + ( (double)abm.state.iocOrders * 100 / abm.state.mkOrdCnt ) + "%)");

                    //System.out.println("# Agent cross limit orders: " + state.agCrossLimOrdCnt + "(" + ( (double)state.agCrossLimOrdCnt * 100 / totalLim ) + "%)");
                    System.out.println("# Agent effective cross limit orders: " + abm.state.effCrossLimOrdCnt + "(" + ( (double)abm.state.effCrossLimOrdCnt * 100 / totalLim ) + "%)");

                    //System.out.println("# Agent in-spread limit orders: " + state.agInSprLimOrdCnt + "(" + ( (double)state.agInSprLimOrdCnt * 100 / totalLim ) + "%)");
                    System.out.println("# Agent effective in-spread limit orders: " + abm.state.effInSprLimOrdCnt + "(" + ( (double)abm.state.effInSprLimOrdCnt * 100 / totalLim ) + "%)");

                    //System.out.println("# Agent spread limit orders: " + state.agSprLimOrdCnt + "(" + ( (double)state.agSprLimOrdCnt * 100 / totalLim ) + "%)");
                    System.out.println("# Agent effective spread limit orders: " + abm.state.effSprLimOrdCnt + "(" + ( (double)abm.state.effSprLimOrdCnt * 100 / totalLim ) + "%)");

                    //System.out.println("# Agent off-spread limit orders: " + state.agOffSprLimOrdCnt + "(" + ( (double)state.agOffSprLimOrdCnt * 100 / totalLim ) + "%)");
                    System.out.println("# Agent effective off-spread limit orders: " + abm.state.effOffSprLimOrdCnt + "(" + ( (double)abm.state.effOffSprLimOrdCnt * 100 / totalLim ) + "%)");

                    System.out.println("# Trades: " + abm.state.tradeCnt);
                    System.out.println("Total trade volume: " + abm.state.tradeVol);
                    System.out.println("Av. trade size: " + ( abm.state.tradeVol / abm.state.tradeCnt ));

                    System.out.println("Av. return: " + ( (double)abm.state.sumRet / abm.state.tradeCnt ));
                    System.out.println("Av. variance: " + ( (double)abm.state.sqSumRet / abm.state.tradeCnt - Math.pow( abm.state.sumRet/abm.state.tradeCnt, 2 ) ) );

                    System.out.println("Instant volatility: " + abm.state.mt.instantVola );

                    System.out.println("Av. spread: " + abm.state.avSpread );
                    System.out.println("Av. perc. spread: " + abm.state.avPercSpread );
                }

                if ( Consts.DEBUGDISK ) {

                    String fileStamp = "_c" + k + "_r" + i + "_" + dateFormat.format(date);
                    
                    abm.state.saveTradesToFile( path + "trades" + fileStamp + ".csv", abm.state.burnInPeriod, true );
                    //abm.state.saveMarketImpactToFile( path + "mkImp" + fileStamp + ".csv", k, i, true );
                    //abm.state.saveOffSpreadRelLimDistToFile( path + "relLimDist" + fileStamp + ".csv", k, i, true );
                }

            } // end for runs: i

        } // end for (random) configurations: k
        
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }
}
