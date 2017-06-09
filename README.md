# payscheduler

a simple clojure library for automating payments using Venmo. You'll need a Venmo account.


See the [Venmo API Developer documentation](https://developer.venmo.com/docs/endpoints/payments)

## Usage

`lein run <config-file-path> <payment-name>`

`java -cp target/ clojure.main -m com.adamtait.payscheduler <config-file> <payment-name>`

## License

Copyright © 2014 Adam Tait

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.


## Update

Sometime in 2016, Venmo changed it's business model and shut down it's API. Unfortunately, this means that _payscheduler_ will no longer work as intended. Venmo now suggests that you use the Paypal API instead; I have no intention of using the Paypal API so I haven't bothered to update _payscheduler_.
