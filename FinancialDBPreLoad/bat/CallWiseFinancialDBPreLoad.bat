REM FinancialDBPreLoad for Wise a csv file

:: clean out the backup folder 
del backup\*.csv

call FinancialDBPreLoad.bat wise
pause

call OneDriveBackup.bat
pause
