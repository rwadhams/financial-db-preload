package com.wadhams.financials.db.preload.app

import com.wadhams.financials.db.preload.controller.ConvertSuncorpDataController
import com.wadhams.financials.db.preload.controller.ConvertWiseDataController
import com.wadhams.financials.db.preload.controller.TestController
import com.wadhams.financials.db.preload.type.Transaction

class ConvertCSV2XMLTestApp {
		
	static main(args) {
		println 'ConvertCSV2XMLTestApp started...'
		println ''

		ConvertCSV2XMLTestApp app =new ConvertCSV2XMLTestApp()
		app.execute()
		
		println ''
		println 'ConvertCSV2XMLTestApp ended.'
	}

	def execute() {
		TestController controller = new TestController()
		controller.researchDataFile('wise-testdata-01.csv')
		
//		controller.analyseDataFile('wise-testdata-all.csv')
	}	
}
