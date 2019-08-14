package org.yoyama.micrometer

import java.time.Duration

import io.micrometer.core.instrument.push.PushRegistryConfig
import io.micrometer.core.instrument.step.StepRegistryConfig

trait FluencyRegistryConfigTrait extends StepRegistryConfig {
  self: PushRegistryConfig =>
  override def prefix(): String = "default"

  def get(key: String): String = {
    key match {
      case "prefix" => prefix
      case "batchSize" => batchSize.toString
      case "tag" => tag
      case _ => null //self.get(key) //throw new RuntimeException(s"No defined key:$key")
    }
  }

  def tag():String
}

class FluencyRegistryConfig(tagValue:String, prefixValue:String, stepValue:Duration) extends FluencyRegistryConfigTrait {
  def tag():String = tagValue

  override def prefix(): String = prefixValue

  override def step(): Duration = stepValue

}

