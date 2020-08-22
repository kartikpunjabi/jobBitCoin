#### **JobBitCoin**

### **Getting Started**
_Build Tools_: Gradle

_IDE Used_: Intellij 2020_

_Modules Used_:
- Springboot
- lombok
- jobDb


I was fiddling with the idea of using in memory jsonDb database to make it complete/ fail safe service. 
This would have enabled service to create error channel and ensured idempotency, but in interest of time
I decided against it.

Run com.mixer.Application in the project, follow the command line instructions enter needed source 
address, deposit address and one or more final destination addresses. Once all the details entered,
transaction will begin. Service should immediately transfer coins from source to deposit and deposit 
to house address. It will also schedule coin transfers equally to all the final destination address.

One caveat, command line doesn't like blank/ space entries.  

### **Jobcoin Algorirthm**

1. Enter/ Pick source address
2. Enter/ Pick deposit address
3. Enter/ Pick final destination address(es) and number of coins to be transferred
4. Service will transfer the coin from source to deposit and deposit to house. 
5. Algo then divides all the coins in equal parts to provided destination address(es)
6. These house to destination address(es) transaction will be posted with difference of 
30 seconds(Ideally it should pick random time interval)






