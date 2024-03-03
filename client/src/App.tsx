import { useEffect } from 'react' //useState } from 'react'
import './App.css'
import CreatePayToWinClient from './paytowinclient'
import { GrpcWebFetchTransport } from '@protobuf-ts/grpcweb-transport'
import { PlayersRequest } from './paytowin'//, PotionName } from './paytowin'


function App() {
  //const [count, setCount] = useState(0)

  const transport = new GrpcWebFetchTransport({
    baseUrl: "http://localhost:50051",
    meta: {
      "password": "password"
    }
  }));

  const client = new PayToWinClient(transport);

  let players: string[] = [];

  useEffect(() => {
    const playerResponse = await client.getPlayers(PlayersRequest.create());
    players = playerResponse.players;
    (async () => {
      try {
          // for await (const {playersList} of playerResponse.players) {
          //   console.log(playersList)
          //   //const player = players[0]
  
          //   /*client.applyEffect({player: player, effect: {
          //     oneofKind: "potion",
          //     potion: {
          //       name: PotionName.JUMP,
          //       duration: 10,
          //       amplifier: 10
          //     }
          //   }}).then(console.log)*/
  
          // }
          console.log(playerResponse);
      } catch (error) {
        console.log(error)
      }
    })()

    
  }, [])


  return (<>
    <div>
      {players.map(player => (<div>{player}</div>))}
    </div>
  </>);
}

export default App
