package com.wadhams.financials.db.preload.controller

import com.wadhams.financials.db.preload.dto.WiseDTO
import com.wadhams.financials.db.preload.service.WiseCSVService
import com.wadhams.financials.db.preload.service.WiseService
import com.wadhams.financials.db.preload.type.Status


class ConvertWiseDataController {
	def execute() {
		WiseCSVService wiseCSVService = new WiseCSVService()
		
		File csvFile = wiseCSVService.getDataFile()
//		println "Wise datafile name: ${csvFile.name}"
//		println ''
		
		List<WiseDTO> wiseDTOList = wiseCSVService.buildWiseDTOList(csvFile)
		println "wiseDTOList size: ${wiseDTOList.size()}"
		println ''
		
		WiseService wiseService = new WiseService()
		List<WiseDTO> combinedWiseDTOList = wiseService.combineDuplicateIds(wiseDTOList)
		println "combinedWiseDTOList size: ${combinedWiseDTOList.size()}"
		println ''

		//filter into two lists
		List<WiseDTO> validDTOList = wiseService.filterDTOList(combinedWiseDTOList, Status.Valid)
		println "validDTOList size: ${validDTOList.size()}"
		List<WiseDTO> bypassDTOList = wiseService.filterDTOList(combinedWiseDTOList, Status.Bypass)
		println "bypassDTOList size: ${bypassDTOList.size()}"
		println ''
		
		wiseService.reportBypassItems(bypassDTOList)

		wiseService.writeValidItemFile(validDTOList)
		
		wiseCSVService.backupDataFile(csvFile)
	}
}
