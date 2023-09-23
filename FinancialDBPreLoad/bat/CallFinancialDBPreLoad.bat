REM FinancialDBPreLoad

:: clean out the backup folder 
del backup\*.csv

call FinancialDBPreLoad.bat
pause

call OneDriveBackup.bat
pause
