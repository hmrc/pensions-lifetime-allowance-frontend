/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package common

import play.api.i18n.Messages
import Array._

object resultMethods{

	def numberOfPara(number: Int, i: Int = 1, paragraphs: Array[String] = Array[String]()): Array[String] = {
	    val x: String = "resultCode." + number.toString() + "." + i.toString()
	    if(Messages(x) == x){
	    	paragraphs
	    } else {
	    	numberOfPara(number, i+1, paragraphs :+ Messages(x))
	    }
	}
}
