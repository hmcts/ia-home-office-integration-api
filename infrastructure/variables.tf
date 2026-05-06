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

# thumbprint of the SSL certificate for API gateway tests
variable "api_gateway_test_certificate_thumbprints" {
  type    = list(any)
  default = [] # TODO: remove default and provide environment-specific values
}

variable "aks_subscription_id" {
}

variable "apim_suffix" {
  default = ""
}

variable "product_name" {
  type    = string
  default = "payments"
}