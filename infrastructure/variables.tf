variable "product" {}

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
