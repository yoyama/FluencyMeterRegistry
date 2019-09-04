package io.github.yoyama.micrometer

import java.time.Duration

import io.micrometer.core.instrument.step.StepRegistryConfig

trait FluencyRegistryConfig extends StepRegistryConfig {
  override def prefix(): String = "default"

  def get(key: String): String = {
    key match {
      case "prefix" => prefix
      case "batchSize" => batchSize.toString
      case "tag" => tag
      case _ => null
    }
  }

  def tag():String

  def disableMetricsTag():Boolean = false
}

object FluencyRegistryConfig {
  def apply(tagValue: String, prefixValue: String, stepValue: Duration, disableMetricsTagValue: Boolean): FluencyRegistryConfig = {
    new FluencyRegistryConfig {
      override def tag(): String = tagValue

      override def prefix(): String = prefixValue

      override def step(): Duration = stepValue

      override def disableMetricsTag(): Boolean = disableMetricsTagValue
    }
  }

  def apply(tagValue: String, prefixValue: String, stepValue: Duration): FluencyRegistryConfig = apply(tagValue, prefixValue, stepValue, false)
}
