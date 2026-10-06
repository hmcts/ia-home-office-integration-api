terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      # Leave this pinned until https://github.com/hashicorp/terraform-provider-azurerm/pull/29523 is merged in
      version = "4.21.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "3.0.2"
    }
  }
}
