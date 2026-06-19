{
	"schemaVersion": 1,
	"id": "visualparticle-better",
	"version": "0.0.3-1.21.11",
	"name": "Visual Particle Better",
	"description": "Client-side particle effects for attacks, walking, projectiles, and totem pops with an in-game Right Shift settings menu.",
	"authors": [
		"Me!"
	],
	"contact": {
		"homepage": "https://fabricmc.net/",
		"sources": "https://github.com/FabricMC/fabric-example-mod"
	},
	"license": "CC0-1.0",
	"icon": "assets/visualparticle-better/icon.png",
	"environment": "*",
	"entrypoints": {
		"main": [
			"particle.fx.ParticleFxMod"
		],
		"client": [
			"particle.fx.ParticleFxClient"
		]
	},
	"mixins": [
		"visualparticle-better.mixins.json",
		{
			"config": "visualparticle-better.client.mixins.json",
			"environment": "client"
		}
	],
	"depends": {
		"fabricloader": ">=0.18.4",
		"minecraft": "~1.21.11",
		"java": ">=21",
		"fabric-api": "*"
	}
}
