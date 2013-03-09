package com.m0pt0pmatt.bettereconomy;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

class BalanceComparator implements Comparator<Account>{
	   
    public int compare(Account account1, Account account2){
   
        /*
         * parameter are of type Object, so we have to downcast it
         * to Employee objects
         */
       
        double emp1Balance = account1.getBalance();        
        double emp2Balance = account2.getBalance();
       
        if(emp1Balance > emp2Balance)
            return 1;
        else if(emp1Balance < emp2Balance)
            return -1;
        else
            return 0;    
    }
   
}

/**
 * Handles all economy related commands, etc
 * @author Matthew
 */
public class EconomyManager implements net.milkbowl.vault.economy.Economy {
	
	/**
	 * List of all currently loaded currencies
	 */
	private List<Currency> currencies;
	
	/**
	 * List of all currently loaded accounts
	 */
	private LinkedList<InventoryAccount> accounts;
	
	/**
	 * starting balance for new accounts
	 */
	public static double startingBalance = 50.0;
	
	private BetterEconomy plugin;
	
	/**
	 * Default Constructor 
	 */
	public EconomyManager(BetterEconomy plugin){
		//create a list for currencies
		currencies = new LinkedList<Currency>();
		
		this.plugin = plugin;
		
		//load accounts from file
		load();
	}
	
	/**
	 * Save economy data to file
	 */
	public void save(){
		try {
			plugin.fileManager.save(accounts, "accounts");
		} catch (IOException e) {
			System.err.println("[HomeWorldPlugin-Economy] Error Saving File");
			e.printStackTrace();
		}
	}

	/**
	 * load economy data from file
	 */
	@SuppressWarnings("unchecked")
	public void load(){
		try {
			accounts = (LinkedList<InventoryAccount>) plugin.fileManager.load("accounts", false);
		} catch (IOException e) {
			System.err.println("[HomeWorldPlugin-Economy] Error Loading File");
			e.printStackTrace();
		}
	}
	
	/**
	 * Add a currency to the list of valid currencies
	 * @param newCurrency the new Currency type to be added
	 */
	public void addCurrency(Currency newCurrency){
		currencies.add(newCurrency);
	}
	
	public Currency getCurrency(String currencyName){
		for (Currency c: currencies){
			if (c.getName().equals(currencyName)){
				return c;
			}
		}
		return null;
	}
	
	/**
	 * Returns the list of currencies
	 * @return the list of currencies
	 */
	public List<Currency> getCurrencies(){
		return currencies;
	}
	
	/**
	 * Checks if the given currency is in the list of valid currencies
	 * @param currency
	 * @return true if currency is a valid currency, false if not
	 */
	public boolean containsCurrency(Currency currency){
		if (currencies.contains(currency)){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Checks if the given currency is in the list of valid currencies
	 * @param currency
	 * @return true if currency is a valid currency, false if not
	 */
	public boolean containsCurrency(String currencyName){
		for (Currency c: currencies){
			if (c.getName().equals(currencyName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the dollar value of a given currency
	 * @param currencyName Name of the currency
	 * @return -1 if invalid, else dollar value
	 */
	public int getCurrencyValue(String currencyName){
		for (Currency c: currencies){
			if (c.getName().equals(currencyName)){
				return c.getValue(1);
			}
		}
		return -1;
	}
	
	/**
	 * gets the Material of a given currency 
	 * @param currencyName Name of the currency
	 * @return null if invalid, else Material of currency
	 */
	public Material getCurrencyMaterial(String currencyName){
		for (Currency c: currencies){
			if (c.getName().equals(currencyName)){
				return c.getMaterial();
			}
		}
		return null;
	}
	
	/**
	 * Checks the value of a given amount of a given currency
	 * @param sender The player executing the command
	 * @param name The name of the currency
	 * @param amount The amount specified
	 */
	public void checkValue(CommandSender sender, String name, int amount){
		if (!(containsCurrency(name))){
			sender.sendMessage("That is not a valid currency");
			return;
		}
		String message = amount + " " + name + " is worth $" + (amount * getCurrencyValue(name));
		sender.sendMessage(message);
	}
	
	/**
	 * Checks if an itemstack is a specified currency
	 * @param stack Itemstack
	 * @param currencyName Currency name
	 * @return true if so, false if not
	 */
	public boolean isCurrency(ItemStack stack, String currencyName){
		if (stack.getType().equals(getCurrencyMaterial(currencyName))){
			if (stack.getData().getData() == getCurrency(currencyName).getMaterialData()){
				return true;
			}
		}
		return false;
	}
	/**
	 * Counts the number of a currency is in a given inventory
	 * @param inv The inventory to be checked
	 * @param currency The currency to be counted
	 * @return The amount of currency in the inventory
	 */
	public int countInventory(Inventory inv, Currency currency){
		
		int i = 0;
		for (ItemStack stack : inv.getContents()){
			if (stack != null){
				if (isCurrency(stack, currency.getName())){
					i += stack.getAmount();
				}
			}
		}
		
		return i;
	}
	
	/**
	 * Counts the number of empty spaces in a given inventory for a currency
	 * @param inv The inventory to be checked
	 * @param currency for which space is being checked
	 * @return Number of empty spaces in the inventory
	 */
	public int countEmptyInventory(Inventory inv, Currency currency){
	
		//check inventory for empty spots
		int i = 0;
		for (ItemStack stack : inv.getContents()){
			if (stack != null){
				if (isCurrency(stack, currency.getName())){
					i += (64 - stack.getAmount());
				}
			}
			else{
				i += 64;
			}
		}
		
		return i;
	}
	
	/**
	 * Adds a number of items to an inventory
	 * @param inv The inventory to be added to
	 * @param currency The currency to be added
	 * @param amount The amount of currency to add
	 */
	 public void withdrawCurrency(Inventory inv, Currency currency, int amount){
	 	
	 	int j = 0;
	 	for (ItemStack stack : inv.getContents()){
			if (stack != null){
				if (isCurrency(stack, currency.getName())){
					//finish off this stack
					if (amount + stack.getAmount() <= 64){
						inv.setItem(j, null);
						inv.addItem(new ItemStack(stack.getType(), stack.getAmount() + amount, (short) 0, currency.getMaterialData()));
						amount = 0;
					}
					//add the whole stack and keep going
					else{
						amount -= (64 - stack.getAmount());
						inv.setItem(j, null);
						inv.addItem(new ItemStack(stack.getType(), 64, (short) 0, currency.getMaterialData()));
					}
				}
			}
			else{
				//finish off this stack
				if (amount <= 64){
					inv.addItem(new ItemStack(getCurrencyMaterial(currency.getName()), amount, (short) 0, currency.getMaterialData()));
					amount = 0;
				}
				//add the whole stack and keep going
				else{
					amount -= 64;
					inv.addItem(new ItemStack(getCurrencyMaterial(currency.getName()), 64, (short) 0, currency.getMaterialData()));
				}
			}
			
			if (amount == 0){
				return;
			}
			j++;
		}
	 }
	
	/**
	 * Allows a player to withdraw an amount of physical currency into his or her inventory, given he or she has the needed funds
	 * @param sender The player executing the command
	 * @param currency The name of the physical currency specified
	 * @param amount The amount of physical currency to be withdrawn
	 */
	public void withdraw(CommandSender sender, String currency, int amount) {
		
		//make sure the amount specified was a positive number
		if (amount <= 0){
			sender.sendMessage("Please enter a positive amount");
			return;
		}
		
		//make sure its a player
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//make sure the player is in the right world
		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
			sender.sendMessage("Sorry, you have to be on the HomeWorld to withdraw");
			return;
		}
		
		//make sure player has enough funds
		if (java.lang.Math.floor(getAccount(sender.getName()).getBalance() / getCurrencyValue(currency)) < amount){
			sender.sendMessage("Sorry, you dont have enough money");
			return;
		}
		
		Inventory playerInventory = ((Player) sender).getInventory();
		
		//checks for enough space to withdraw
		if (countEmptyInventory(playerInventory, this.getCurrency(currency)) < amount){
			sender.sendMessage("Sorry, there is not enough space in your inventory.");
			return;
		}
		
		//add items
		int i = amount;
		withdrawCurrency(playerInventory,  this.getCurrency(currency), i);
		
		//remove funds
		getAccount(sender.getName()).withdraw(amount * getCurrencyValue(currency));
		sender.sendMessage(amount + " " + currency + " was withdrawn.");
		return;
		
		//hopefully, this statement never runs
		//sender.sendMessage("something bad happened, but at least you are not running");
	}

	/**
	 * Greedily withdraws a currency from a sender's account
	 * @param sender The individual sending the command
	 * @param currency The currency to be withdrawn
	 */
	public void greedyWithdraw(CommandSender sender, Currency currency){
		int amount = (int) java.lang.Math.floor(getAccount(sender.getName()).getBalance() / getCurrencyValue(currency.getName()));
	
		Inventory playerInventory = ((Player) sender).getInventory();
		
		//checks for max space for currency withdrawl
		int i = amount;
		if (countEmptyInventory(playerInventory, currency) < amount){
			i = countEmptyInventory(playerInventory, currency);
		}
		
		withdrawCurrency(playerInventory, currency, i);
		
		//remove funds
		getAccount(sender.getName()).withdraw(i * getCurrencyValue(currency.getName()));
		sender.sendMessage(i + " " + currency + " was withdrawn.");
		return;
	}
	
	/**
	 * Withdraws as much currency greedily as possible from a sender's account
	 * @param sender The individual withdrawing the currency
	 */
	public void withdrawAll(CommandSender sender){
		
		//make sure its a player
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//make sure the player is in the right world
		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
			sender.sendMessage("Sorry, you have to be on the HomeWorld to withdraw");
			return;
		}
		
		//make sure the player has currency to withdraw
		if(getAccount(sender.getName()).getBalance() == 0){
			sender.sendMessage("Sorry, you're broke");
		}
		
		//greedy algorithm iteration through arraylist withdrawing items
		for(Currency currency: this.getCurrencies()){
			greedyWithdraw(sender, currency);
		}
		
		return;
	}
	
	/**
	 * Returns a Players Account
	 * @param name Name of the player
	 * @return Player's account
	 */
	public Account getAccount(String name) {
		for (Account a: accounts){
			if (a.getOwner().equals(name)){
				return a;
			}
		}
		return null;
	}
	
	/**
	 * Checks if a player has an account
	 * @param name Name of the player
	 * @return true if player has an account, false if not
	 */
	public boolean hasAccount(String name){
		for (Account a: accounts){
			if (a.getOwner().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds an account
	 * @param account Account to be added
	 */
	public void addAccount(InventoryAccount account){
		accounts.add(account);
	}

	/**
 	* Removes an amount of currency from a given inventory
 	* @param inv The inventory from which the currency is removed
 	* @param currency The currency to be removed
 	* @amount The ammount of currency to be removed
 	*/
	public void depositCurrency(Inventory inv, Currency currency, int amount){
	
		int i = amount;
		int j = 0;
		for (ItemStack stack : inv.getContents()){
			if (stack != null){
				if (isCurrency(stack, currency.getName())){
					//finish off this stack
					if (i < stack.getAmount()){
						
						stack.setAmount(stack.getAmount() - i);
						i = 0;
						inv.setItem(j, new ItemStack(stack.getType(), stack.getAmount(), (short) 0, currency.getMaterialData()));
						
					}
					//remove the whole stack and keep going
					else{
						i -= stack.getAmount();
						inv.setItem(j, null);
					}
					
					if (i == 0){
						return;
					}
				}
			}
			j++;
		}
	}
	
	/** 
	 *  @Author Timmy Miles
	 *  Allows a player to deposit all currency in his/her inventory to his/her account
	 *  Derivation of Lucas's addition using an arraylist
	 *  @param sender The player executing the command
	 *  @return void
	 */
	 public void depositEverything(CommandSender sender){
	 	
	 	// Iterates through arraylist depositing items
	 	for(Currency currency: this.getCurrencies()){
	 		depositAll(sender, currency.getName());	 	
	 	}
	 
	 	return; 
	 }
	
	/**
	 * Allows a player to deposit an amount of physical currency into his or her account, given he or she has the currency
	 * @param sender The player executing the command
	 * @param currency The name of the physical currency specified
	 * @param amount The amount of physical currency to be deposited
	 */
	public void deposit(CommandSender sender, String currency, int amount){
		
		//make sure amount specified was a positive number
		if (amount <= 0){
			sender.sendMessage("Please enter a positive amount");
			return;
		}
		
		//make sure its a player
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//make sure the player is in the right world
		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
			sender.sendMessage("Sorry, you have to be on the HomeWorld to deposit items");
			return;
		}
		
		//get the players inventory
		Inventory playerInventory = ((Player) sender).getInventory();
		
		//check inventory
		int i = countInventory(playerInventory, this.getCurrency(currency));
		
		//make sure enough was found
		if (i < amount){
			sender.sendMessage("Sorry, you dont have enough " + currency);
			return;
		}
		
		//remove items
		depositCurrency(playerInventory, this.getCurrency(currency), amount);
						
		//add funds
		getAccount(sender.getName()).deposit(amount * getCurrencyValue(currency));
		sender.sendMessage(amount + " " + currency + " was deposited.");
		return;

		//hopefully, this statement never executes
		//sender.sendMessage("something bad happened, we're screwed");
		
	}
	
	/**
	 * Deposits all instances of a currency in the sender's inventory
	 * @param sender The individual sending the command
	 * @param currency The currency to be deposited
	 */
	public void depositAll(CommandSender sender, String currency){
		
		//make sure its a player
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//make sure the player is in the right world
		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
			sender.sendMessage("Sorry, you have to be on the HomeWorld to deposit items");
			return;
		}
		
		//get the players inventory
		Inventory playerInventory = ((Player) sender).getInventory();
		
		//check inventory
		int amount = countInventory(playerInventory, this.getCurrency(currency));
		
		depositCurrency(playerInventory, this.getCurrency(currency), amount);
						
		//add funds
		getAccount(sender.getName()).deposit(amount * getCurrencyValue(currency));
		sender.sendMessage(amount + " " + currency + " was deposited.");
		
		//hopefully, this statement never executes
		sender.sendMessage("something bad happened, we're screwed");
	}

	/**
	 * Calculates the sum of all physical currencies in the player's inventory
	 * @param sender The player executing the command
	 */
	public int calculateWealth(Inventory playerInventory){
		
		//calculate wealth
		int wealth = 0;
		for (ItemStack stack : playerInventory.getContents()){
			if (stack != null){
				for (Currency c: currencies){
					if (isCurrency(stack, c.getName())){
						wealth += c.getValue(stack.getAmount());
					}
				}
			}
		}
		
		return wealth;
	}
	
	
	public void calculateWealth(CommandSender sender){
		
		//make sure its a player
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, only players can execute this command");
			return;
		}
		
		//get the players inventory
		Inventory playerInventory = ((Player) sender).getInventory();
		
		//tell the player
		sender.sendMessage("You are carrying $" + calculateWealth(playerInventory) + " worth in materials");
	}

	/**
	 * Shows a player his or her current balance
	 * @param sender The player executing the command
	 */
	public void showBalance(CommandSender sender) {
		if (!(hasAccount(sender.getName()))){
			sender.sendMessage("Error: for some reason you do not have an account");
			return;
		}
		sender.sendMessage("Your current balance is: $" + getAccount(sender.getName()).getBalance());
	}

	/**
	 * Gets the balance of a given player
	 * @param name Name of the player
	 * @return player's balance in dollars
	 */
	public double getBalance(String name) {
		if (!(hasAccount(name))){
			return -1;
		}
		return getAccount(name).getBalance();
	}



	/**
	 * Sets a players balance. Can only be executed by the server
	 * @param server Server
	 * @param player Player to set balance
	 * @param amount amount to be set
	 */
	public void setBalance(CommandSender server, String player, double amount) {

		//make sure its the server
		if (server instanceof Player){
			server.sendMessage("Sorry, only the server can execute this command");
			return;
		}
		
		//check if player has an account
		for (InventoryAccount a: accounts){
			if (a.getOwner().equals(player)){
				//set and return
				a.setBalance(amount);
				server.sendMessage("Account was set");
				return;
			}
		}
		
		//create account since player doesn't already have one
		InventoryAccount account = new InventoryAccount(player, amount);
		
		//add to accounts
		accounts.add(account);
		
		server.sendMessage("Account was created an set");
	}

	public void pay(CommandSender sender, String receiver, double amount) {

		if (!hasAccount(sender.getName())){
			sender.sendMessage("I'm sorry, but you do not have an account");
			return;
		}
		
		if (!hasAccount(receiver)){
			sender.sendMessage("I'm sorry, but " + receiver + " does not have an account");
			return;
		}
		
		//make sure sender has enough money
		Account sendersAccount = getAccount(sender.getName());
		if (sendersAccount.getBalance() < amount){
			sender.sendMessage("I'm sorry, but you do not have that much money in your account");
			return;
		}
		
		//take money from the sender
		sendersAccount.withdraw(amount);
		
		//pay the player
		Account receiverAccount = getAccount(receiver);
		receiverAccount.deposit(amount);
		
		//notify both players
		sender.sendMessage("You have payed " + receiver + " $" + amount + " dollars");
		Bukkit.getPlayer(receiver).sendMessage("" + sender.getName() + " has payed you $" + amount + " dollars");
		
	}

	public void top(CommandSender sender, int number) {

		sender.sendMessage("here are the top " + number + " accounts on the server:");
		
		java.util.Collections.sort(accounts, new BalanceComparator());
		java.util.Collections.reverse(accounts);
		
		int i = 0;
		for (InventoryAccount a: accounts){
			sender.sendMessage(a.getOwner() + ": " + a.getBalance());
			i++;
			if (i == number){
				return;
			}
		}
		
	}

	
	
	
	public void reduceInventory(Inventory inv, int reduce) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * Here starts the code for the interface to vault.
	 * These methods are what make the EconomyManager an actual Economy
	 * These methods need to be filled
	 */

	public EconomyResponse bankBalance(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public EconomyResponse bankDeposit(String arg0, double arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public EconomyResponse bankHas(String arg0, double arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public EconomyResponse bankWithdraw(String arg0, double arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public EconomyResponse createBank(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean createPlayerAccount(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public String currencyNamePlural() {
		// TODO Auto-generated method stub
		return null;
	}

	public String currencyNameSingular() {
		// TODO Auto-generated method stub
		return null;
	}

	public EconomyResponse deleteBank(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public EconomyResponse depositPlayer(String arg0, double arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String format(double arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int fractionalDigits() {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<String> getBanks() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean has(String arg0, double arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasBankSupport() {
		// TODO Auto-generated method stub
		return false;
	}

	public EconomyResponse isBankMember(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public EconomyResponse isBankOwner(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	
	/**
	 * Removes a given amount of money from a player's account
	 * @param name Name of the player
	 * @param amount amount to be removed
	 */
	public EconomyResponse withdrawPlayer(String name, double amount) {		
		if (!(hasAccount(name))){
			return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "No such account exists");
		}
		Account account = getAccount(name);
		account.withdraw(amount);
		return new EconomyResponse(amount, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, "Successfully took "+ amount +" from " + name);
	}
	
	/**
	 * Adds a given amount of money to a player's account
	 * @param name Name of the player
	 * @param amount amount to be added
	 */
	public void deposit(String name, double amount) {
		if (!(hasAccount(name))){
			return;
		}
		Account account = getAccount(name);
		account.deposit(amount);
	}
	
}