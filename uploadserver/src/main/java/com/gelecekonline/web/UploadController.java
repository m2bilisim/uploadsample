/*******************************************************************************
 * Copyright 2012 Muharrem Tac
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.gelecekonline.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.gelecekonline.web.model.Talep;
import com.gelecekonline.web.model.Yanit;

@Controller
public class UploadController {

	/**
	 * Örneğimizde dizin adi C:/uploadornek olarak belirlendi ancak siz bu dizin adini degistirebilirsiniz
	 */
	private static final String DIZIN_ADI = "C:/uploadornek";

	/**
	 * API'ye Android uygulamasi tarafindan soyle erisilecektir: http://localhost:8080/uploadserver/api/upload
	 * @param talep
	 * @param result
	 * @param request
	 * @return
	 */
	@RequestMapping("/api/upload")
	@ResponseBody
	public Yanit uploadGonder(Talep talep, BindingResult result, HttpServletRequest request){
		
		Yanit yanit= null;
		try {
			/**
			 * Istemci tarafindan gonderilen dosyanin da ayni isimde bir veri icermesi gerekiyor.Yani "dosya" ve "dosyaAdi" seklinde gonderilmelidir
			 * dosyaAdi ile istemciden sunucuya Android cihazdaki dosya adini da gonderebiliyoruz. Bunun yaninda Talep nesnesine
			 * baska parametreler ekleyerek cok sayida bilgi de gonderebiliriz
			 */
			String dosyaAdi = talep.getDosyaAdi();
			CommonsMultipartFile dosya = talep.getDosya();
			if(dosya!=null){
				InputStream inputStream = dosya.getInputStream();
				File dizin = new File(DIZIN_ADI);
				if (!dizin.exists())
				  {
				    System.out.println("dizin yoktu, olusturuluyor: " + DIZIN_ADI);
				    boolean res = dizin.mkdir();  
				    if(res){    
				       System.out.println("dizin yaratildi");  
				     }

				  }
				OutputStream out = new FileOutputStream(new File(DIZIN_ADI+"/"+dosyaAdi));
				int read = 0;
				byte[] bytes = new byte[1024];
			 
				while ((read = inputStream.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				inputStream.close();
				out.flush();
				out.close();
				yanit = new Yanit();
				yanit.setYanitMesaji("Upload gerceklesti");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return yanit;
	
	}
}
