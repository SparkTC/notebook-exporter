/*
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
 *
 */
package com.stc.tools.notebook.exporter

import java.io.BufferedOutputStream
import java.net.{URL, URLClassLoader}
import java.nio.file.{Files, Paths}

import scala.reflect.internal.util.{BatchSourceFile, Position, SourceFile}
import scala.reflect.io.{VirtualDirectory, VirtualFile}
import scala.reflect.runtime._
import scala.tools.nsc.Global
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.ConsoleReporter
import scalax.file.Path

class RuntimeCompiler (targetDirectory: VirtualDirectory) {

  val settings = new Settings
  settings.usejavacp.value = true
  settings.outputDirs.setSingleOutput(targetDirectory)

  /** Custom reporter that displays the whole source file
    * (with line-numbers) on failure. */
  lazy val reporter = new ConsoleReporter(settings) {
    override def printMessage(pos: Position, msg: String) = {
      println(">>> " + pos + " - " + msg ) // scalastyle:ignore

      //print content with line numbers
      val src = new String(pos.source.content)
      val message = s"----- begin ${pos.source.path} -----\n" +
        src.split("\n").zipWithIndex.map{ case (line, idx) =>
          f"${idx+1}%04d: $line" // +1 since lines are start at 1
        }.mkString("\n") +
        s"\n----- end ${pos.source.path} -----\n"
      println(message) //scalastyle:ignore
    }
  }

  lazy val compiler = new Global(settings, reporter)

  def compile(sourceFile: SourceFile): Unit = {
    (new compiler.Run).compileSources(List(sourceFile))
  }
}

