package com.wadhams.financials.db.preload.app

import com.wadhams.financials.db.preload.controller.ConvertSuncorpDataController
import com.wadhams.financials.db.preload.controller.ConvertWiseDataController
import com.wadhams.financials.db.preload.type.Transaction



class ConvertCSV2XMLApp {
		
	static main(args) {
		println 'ConvertCSV2XMLApp started...'
		println ''

		if (args.size() > 0) {
			Transaction run = Transaction.findByName(args[0])
			println "Transaction parameter: $run"
			println ''
			if (run == Transaction.Suncorp) {
				ConvertSuncorpDataController convertSuncorpDataController = new ConvertSuncorpDataController()
				convertSuncorpDataController.execute()
			}
			else if (run == Transaction.Wise) {
				ConvertWiseDataController convertWiseDataController = new ConvertWiseDataController()
				convertWiseDataController.execute()
			}
			else {
				println 'Unknown parameter. Application did not run.'
			}
		}
		else {
			println 'Missing parameter(s). Application did not run.'
		}

		println ''
		println 'ConvertCSV2XMLApp ended.'
	}
	
}
