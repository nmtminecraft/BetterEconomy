package com.m0pt0pmatt.bettereconomy.banks;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.m0pt0pmatt.bettereconomy.BetterEconomy;
import com.m0pt0pmatt.bettereconomy.currency.Currency;


/**
 * Banks are structures which trade currencies
 * @author Matthew
 *
 */
public class Bank {

	
	//A map from a currency to the amount of that currency currently stored
	private HashMap<Currency,Integer> amounts;
	
	private File configFile;
	private YamlConfiguration config;
	
	public Bank(BetterEconomy plugin, String configName){
		amounts = new HashMap<Currency,Integer>();
		configFile = new File(plugin.getDataFolder(), configName);	
		load();
	}
	
	public void load(){
		config = YamlConfiguration.loadConfiguration(configFile);
		ConfigurationSection currenciesSection = config.getConfigurationSection("currencies");
		if (currenciesSection == null){
			return;
		}
		
		for (String currency: currenciesSection.getKeys(false)){
			ConfigurationSection currencySection = currenciesSection.getConfigurationSection(currency);
			if (currencySection == null) continue;
			if (!currencySection.isInt("amount")) continue;
			int amount = currencySection.getInt("amount");
			amounts.put(BetterEconomy.economy.getCurrency(currency), amount);
		}
		
	}
	
	public void save(){
		ConfigurationSection currenciesSection = config.createSection("currencies");
		for (Entry<Currency, Integer> entry: amounts.entrySet()){
			ConfigurationSection currencySection = currenciesSection.createSection(entry.getKey().getName());
			currencySection.set("amount", entry.getValue());
		}
		try {
			config.save(configFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @return map of currencies to their amounts
	 */
	public HashMap<Currency,Integer> getMap(){
		return amounts;
	}
	
	/**
	 * Adds a currency to the bank (initial amount = 0)
	 * @param c currency to be added
	 */
	public void addCurrency(Currency c){
		amounts.put(c,0);
	}
	
	/**
	 * @param c currency whose amount is desired
	 * @return amount of a currency in the bank, null if currency is not in the bank
	 */
	public int getCurrencyAmount(Currency c){
		
		if (amounts.containsKey(c)){
			return amounts.get(c);
		}
		
		return 0;
		
	}
	
	/**
	 * Removes a currency from the bank
	 * @param c currency to be removed
	 */
	public void removeCurrency(Currency c){
		amounts.remove(c);
	}
	
	/**
	 * Updates the amount of a given currency
	 * @param c currency to be updated
	 * @param amount new amount of the currency
	 */
	public void updateAmount(Currency c, Integer amount){
		amounts.put(c,amount);
	}
}
