variable "product" {}

variable "raw_product" {
  default = "ia" // jenkins-library overrides product for PRs and adds e.g. pr-123-ia
}

variable "component" {}

variable "location" {
  default = "UK South"
}

variable "deployment_namespace" {
  type = "string"
  default = ""
}

variable "env" {}

variable "subscription" {}

variable "common_tags" {
  type = "map"
}

variable "appinsights_instrumentation_key" {
  default = ""
}
