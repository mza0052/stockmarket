package pkg.trader;

import java.util.ArrayList;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.order.BuyOrder;
import pkg.order.Order;
import pkg.order.OrderType;
import pkg.order.SellOrder;

public class Trader {
	// Name of the trader
	String name;
	// Cash left in the trader's hand
	double cashInHand;
	// Stocks owned by the trader
	ArrayList<Order> position;
	// Orders placed by the trader
	ArrayList<Order> ordersPlaced;

	public Trader(String name, double cashInHand) {
		super();
		this.name = name;
		this.cashInHand = cashInHand;
		this.position = new ArrayList<Order>();
		this.ordersPlaced = new ArrayList<Order>();
	}

	public void buyFromBank(Market m, String symbol, int volume)
			throws StockMarketExpection {
        double price =m.getStockForSymbol(symbol).getPrice();
        double neededMoney=price*volume;
       
        if (neededMoney>this.cashInHand)
        	throw new StockMarketExpection("Cannot place order for stock:"+ symbol+ "since there is not enough money.");

       	this.cashInHand-=neededMoney;
        BuyOrder order =new BuyOrder(symbol, volume, price, this);
		position.add(order);
	}

	public void placeNewOrder(Market m, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
		Order order;
		if (orderType==OrderType.BUY){	
	        if (price*volume>this.cashInHand)
	        	throw new StockMarketExpection("Cannot place order for stock:"+ symbol+ "since there is not enough money.");
	        
	        if (indexInTheArr(symbol,ordersPlaced)>=0)
	        	throw new StockMarketExpection("There are multiple orders for the same stock");
			
			order =new BuyOrder(symbol, volume, price, this);
			
		} 		
		else{		
			if(indexInTheArr(symbol, position)==-1)
				throw new StockMarketExpection("Cannot place a sell order for a stock that he does not own.");
			
			order =new SellOrder(symbol, volume, price, this);
		}
		
		ordersPlaced.add(order);
		m.addOrder(order);
	}

	private int indexInTheArr(String symbol, ArrayList<Order> list) throws StockMarketExpection {
		for(Order ord :list)
			if(ord.getStockSymbol()==symbol)
				return list.indexOf(ord);
		return -1;
	}

	public void placeNewMarketOrder(Market m, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
		
			Order order;
			if (orderType==OrderType.BUY){
				
		        if (price*volume>this.cashInHand)
		        	throw new StockMarketExpection("Cannot place order for stock:"+ symbol+ "since there is not enough money.");
		        
		        if (indexInTheArr(symbol,ordersPlaced)>=0)
		        	throw new StockMarketExpection("There are multiple orders for the same stock");
		        if (price==0)
					price=10000;
				order =new BuyOrder(symbol, volume, price, this);} 		
			else{		
				if(indexInTheArr(symbol, position)==-1)
					throw new StockMarketExpection("Cannot place a sell order for a stock that he does not own.");
				
				order =new SellOrder(symbol, volume, price, this);
			}
			
			ordersPlaced.add(order);
			m.addOrder(order);
	}

	public void tradePerformed(Order o, double matchPrice)
			throws StockMarketExpection {
		// Notification received that a trade has been made, the parameters are
		// the order corresponding to the trade, and the match price calculated
		// in the order book. Note than an order can sell some of the stocks he
		// bought, etc. Or add more stocks of a kind to his position. Handle
		// these situations.

		if (o instanceof BuyOrder){
				if (matchPrice*o.getSize()>this.cashInHand)
		        	throw new StockMarketExpection("Cannot place order for stock:"+ o.getStockSymbol()+ "since there is not enough money.");
		         
				this.cashInHand-=matchPrice*o.getSize();
				
				
				int index=indexInTheArr(o.getStockSymbol(), position);
				if (index==-1)
					position.add(o);
				else{
					//BuyOrder newO=new BuyOrder(o.getStockSymbol(), o.getSize()+position.get(index).getSize(), o.getPrice(), o.getTrader());
					//position.set(index, newO);
					int newSze=o.getSize()+position.get(index).getSize();
					position.get(index).setSize(newSze);
				}
				ordersPlaced.remove(o);
		}
		else{
				int index=indexInTheArr(o.getStockSymbol(), position);
				int newSze=position.get(index).getSize()-o.getSize();
				if(newSze<0)
					throw new StockMarketExpection("Not enough stock size");
				if(newSze>0){					
					position.get(index).setSize(newSze);}
				else
					position.remove(index);
				this.cashInHand+=matchPrice*o.getSize();
				ordersPlaced.remove(o);
		}
		// Update the trader's orderPlaced, position, and cashInHand members
		// based on the notification.
	}

	public void printTrader() {
		System.out.println("Trader Name: " + name);
		System.out.println("=====================");
		System.out.println("Cash: " + cashInHand);
		System.out.println("Stocks Owned: ");
		for (Order o : position) {
			o.printStockNameInOrder();
		}
		System.out.println("Stocks Desired: ");
		for (Order o : ordersPlaced) {
			o.printOrder();
		}
		System.out.println("+++++++++++++++++++++");
		System.out.println("+++++++++++++++++++++");
	}
}
