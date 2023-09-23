@echo off
REM OneDriveBackup for FinancialDBPreLoad data files

if not exist "%userprofile%\OneDrive\Documents\App_Data_and_Reporting_Backups\FinancialDBPreLoad\" mkdir %userprofile%\OneDrive\Documents\App_Data_and_Reporting_Backups\FinancialDBPreLoad

xcopy backup\*.csv %userprofile%\OneDrive\Documents\App_Data_and_Reporting_Backups\FinancialDBPreLoad\backup /I /Y
