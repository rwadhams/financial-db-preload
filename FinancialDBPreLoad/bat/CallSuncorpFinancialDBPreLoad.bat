REM FinancialDBPreLoad for Suncorp a csv file

:: clean out the backup folder 
del backup\*.csv

call FinancialDBPreLoad.bat suncorp
pause

call OneDriveBackup.bat
pause
