package io.github.yoyama.micrometer

import java.time.Duration

import io.micrometer.core.instrument.step.StepRegistryConfig

class FluencyRegistryConfig(val prefixValue:String, val tag:String, val disableMetricsTag:Boolean)
                                                                          extends StepRegistryConfig {
  override def prefix(): String = prefixValue

  def get(key: String): String = {
    key match {
      case "prefix" => prefix
      case "batchSize" => batchSize.toString
      case "tag" => tag
      case _ => null
    }
  }
}

object FluencyRegistryConfig {
  def apply(tagValue: String, prefixValue: String, stepValue: Duration, disableMetricsTagValue: Boolean): FluencyRegistryConfig = {
    new FluencyRegistryConfig(prefixValue, tagValue, disableMetricsTagValue) {
      override def step(): Duration = stepValue
    }
  }

  def apply(tagValue: String, prefixValue: String, stepValue: Duration): FluencyRegistryConfig = apply(tagValue, prefixValue, stepValue, false)
}
