FinancialDBPreLoad App
======================

Setup
-----
After deploying App (Unzipping), run OneTimeSetup.bat

Execution
---------
1	Copy downloaded Suncorp csv file to bin folder.
		> (e.g. From: C:\Users\rober\Downloads\Csv20230816.csv, To: C:\Temp\FinancialDBPreLoad\bin\).

2a	Delete the existing 'financial.csv' file in the bin folder.
2b	Rename the copied csv file to 'financial.csv'.

3	Run CallFinancialDBPreLoad.bat
		> verify script output.

4	Verify the datetimestamped csv file in the backup folder.
5	Comfirm that this file has been copied to %userprofile%\OneDrive\Documents\App_Data_and_Reporting_Backups\FinancialDBPreLoad\backup

6	Verify out\financial.xml exists.
		> This file will require formatting and data entry before being used in the FinancialDBLoad App. 
