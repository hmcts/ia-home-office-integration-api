variable "product" {
}

variable "raw_product" {
  default = "ia" // jenkins-library overrides product for PRs and adds e.g. pr-123-ia
}

variable "component" {
}

variable "location" {
  default = "UK South"
}


variable "env" {
}

variable "subscription" {
}

variable "common_tags" {
  type = map(string)
}

variable "appinsights_instrumentation_key" {
  default = ""
}

variable "root_logging_level" {
  default = "INFO"
}

variable "log_level_spring_web" {
  default = "INFO"
}

variable "log_level_ia" {
  default = "INFO"
}
