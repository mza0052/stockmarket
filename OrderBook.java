package pkg.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


import pkg.exception.StockMarketExpection;
import pkg.market.Market;

public class OrderBook {
	Market m;
	HashMap<String, ArrayList<Order>> buyOrders;
	HashMap<String, ArrayList<Order>> sellOrders;

	public OrderBook(Market m) {
		this.m = m;
		buyOrders = new HashMap<String, ArrayList<Order>>();
		sellOrders = new HashMap<String, ArrayList<Order>>();
	}

	public void addToOrderBook(Order order) {
		if (BuyOrder.class.isInstance(order)){
			if (this.buyOrders.containsKey(order.getStockSymbol())){
				ArrayList<Order> list =buyOrders.get(order.getStockSymbol());
				list.add(order);
			}
			else{
				ArrayList<Order> list = new ArrayList<Order>();
				list.add(order);
				this.buyOrders.put(order.getStockSymbol(), list);
			}
		}
		else{
			if (this.sellOrders.containsKey(order.getStockSymbol())){
				ArrayList<Order> list =sellOrders.get(order.getStockSymbol());
				list.add(order);
			}
			else{
				ArrayList<Order> list = new ArrayList<Order>();
				list.add(order);
				this.sellOrders.put(order.getStockSymbol(), list);
			}
		}

		
	}

	public void trade() {
		// Complete the trading.
		// 1. Follow and create the orderbook data representation (see spec)
		// 2. Find the matching price
		// 3. Update the stocks price in the market using the PriceSetter.
		// Note that PriceSetter follows the Observer pattern. Use the pattern.
		// 4. Remove the traded orders from the orderbook
		// 5. Delegate to trader that the trade has been made, so that the
		// trader's orders can be placed to his possession (a trader's position
		// is the stocks he owns)
		// (Add other methods as necessary)
		for (String symbol : this.buyOrders.keySet()){
			if (this.sellOrders.containsKey(symbol)){
				HashMap<Double, ArrayList<Integer>> orderbook=orderBookCreator(symbol);
				double matchPrice=matchPrice(orderbook);
				if(matchPrice>0)
					try{
					m.updateStockPrice(symbol, matchPrice);}
				catch (StockMarketExpection e){};
				
				for (Order sellOrder : this.sellOrders.get(symbol)){
			    	if (sellOrder.getPrice()<=matchPrice){
			    		try {
							sellOrder.getTrader().tradePerformed(sellOrder, matchPrice);
						} catch (StockMarketExpection e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			    		this.sellOrders.remove(sellOrder);
			    	}
			    }
				
				for (Order buyOrder : this.buyOrders.get(symbol)){
			    	if (buyOrder.getPrice()>=matchPrice){
			    		try {
							buyOrder.getTrader().tradePerformed(buyOrder, matchPrice);
						} catch (StockMarketExpection e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			    		this.buyOrders.remove(buyOrder);
			    	}
			    }

				
				
				
			}
			
			
					
				
				
			}
		
	}

	private double matchPrice(HashMap<Double, ArrayList<Integer>> orderbook) {
		ArrayList<Double> priceList=new ArrayList<Double>(); 
		
		for(double prce:orderbook.keySet()){
			priceList.add(prce);
		}
		Collections.sort(priceList, Collections.reverseOrder());
		 
		 ArrayList<Integer> buyCum=new ArrayList<Integer>(priceList.size()); 
		 ArrayList<Integer> sellCum=new ArrayList<Integer>(priceList.size()); 
		 
		 double key=priceList.get(0);
		 buyCum.add(orderbook.get(key).get(0));
			 
		 for(int i=1; i<priceList.size(); i++){
			 key=priceList.get(i);
			 buyCum.add(orderbook.get(key).get(0)+buyCum.get(i-1));
			 }		 
		 
		 
		 key=priceList.get(priceList.size()-1);
		 sellCum.add(orderbook.get(key).get(1));
		 
		 
		 for(int i=priceList.size()-2;i>-1; i--){
			 key=priceList.get(i);
			 sellCum.add(orderbook.get(key).get(1)+sellCum.get(priceList.size()-i-2));
			 }		
		 
		 
		 double matchPrice=0;
		 int max=0;
		 for(int i=0; i<priceList.size(); i++){
			 int min=Math.min(sellCum.get(i),buyCum.get(priceList.size()-1-i));
			 if (min>max){
				 max=min;
				 matchPrice=priceList.get(i);
			 }
			 }
		 
		 return matchPrice;
	}

	private HashMap<Double, ArrayList<Integer>> orderBookCreator(String symbol) {
		HashMap<Double, ArrayList<Integer>> orderbook = new HashMap<Double, ArrayList<Integer>>();
		ArrayList<Order> buyList = this.buyOrders.get(symbol);
		ArrayList<Order> sellList = this.sellOrders.get(symbol);
		for (Order o : buyList){
			double key=o.getPrice();
			ArrayList<Integer> temp=new ArrayList<Integer>();
			if (orderbook.containsKey(key)){						
				temp.add(o.getSize()+orderbook.get(key).get(0));
				temp.add(orderbook.get(key).get(1));
				orderbook.replace(key, temp);
			}
			else{						
				temp.add(o.getSize());
				temp.add(0);
				orderbook.put(key, temp);
				}
			}
		
		for (Order o : sellList){
			double key=o.getPrice();
			ArrayList<Integer> temp=new ArrayList<Integer>();
			if (orderbook.containsKey(key)){	
				temp.add(orderbook.get(key).get(0));
				temp.add(o.getSize()+orderbook.get(key).get(1));
				orderbook.replace(key, temp);
			}
			else{
				temp.add(0);
				temp.add(o.getSize());
				orderbook.put(key, temp);
				}
			}
		return orderbook;
	}

}
