import express from 'express';
import { DataExchangeClient, SendApiAssetCommand } from '@aws-sdk/client-data-exchange';
const app = express();
const client = new DataExchangeClient({ region: process.env.AWS_REGION || 'us-east-1' });
app.get('/metadata', async (req, res) => {
  const title = String(req.query.title || '').trim();
  if (!title) return res.status(400).json({ error: 'title is required' });
  try {
    // IMDb's exact GraphQL search fields depend on your subscribed product/schema.
    // Put the approved query in IMDB_SEARCH_QUERY and adapt mapping below after onboarding.
    const query = (process.env.IMDB_SEARCH_QUERY || '').replaceAll('$TITLE', title.replaceAll('"', '\\"'));
    if (!query) return res.status(501).json({ error: 'Set IMDB_SEARCH_QUERY from your licensed IMDb API schema.' });
    const out = await client.send(new SendApiAssetCommand({
      DataSetId: process.env.IMDB_DATASET_ID, RevisionId: process.env.IMDB_REVISION_ID, AssetId: process.env.IMDB_ASSET_ID,
      Method: 'POST', Path: '/', RequestHeaders: { 'x-api-key': process.env.IMDB_API_KEY }, Body: JSON.stringify({ query })
    }));
    const raw = JSON.parse(Buffer.from(await out.Body.transformToByteArray()).toString('utf8'));
    res.json(raw.data?.mainSearch?.edges?.[0]?.node || raw.data || raw);
  } catch (e) { res.status(502).json({ error: e.message }); }
});
app.listen(process.env.PORT || 8787, () => console.log('IMDb proxy listening'));
