resource "aws_dynamodb_table" "object-name-table" {
  name           = "objectName"
  billing_mode   = "PAY_PER_REQUEST"

  hash_key       = "key"

  attribute {
    name = "key"
    type = "S"
  }
}