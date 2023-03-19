# LINZ Address Parser

This repo contains code that can be used to train a bi-directional LSTM model to parse address strings, using [LINZ addresses](https://data.linz.govt.nz/layer/105689-nz-addresses/) as training data.  E.g.

![](img/parsed.png)

The method is similar to [AddressNet](https://github.com/jasonrig/address-net) for Australian addresses.

**n.b.** I had issues running this with CUDA on bare metal since my CUDA version wasn't supported by Deeplearning4j.  You can use older CUDA versions inside Docker containers, hence the provided `docker-compose.yml` file.